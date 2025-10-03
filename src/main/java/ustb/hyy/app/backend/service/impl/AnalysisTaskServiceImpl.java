package ustb.hyy.app.backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ustb.hyy.app.backend.cache.TaskProgressCache;
import ustb.hyy.app.backend.common.exception.BusinessException;
import ustb.hyy.app.backend.common.exception.ResourceNotFoundException;
import ustb.hyy.app.backend.common.response.PageResult;
import ustb.hyy.app.backend.common.util.VideoUtils;
import ustb.hyy.app.backend.domain.entity.*;
import ustb.hyy.app.backend.mq.message.VideoAnalysisMessage;
import ustb.hyy.app.backend.mq.producer.VideoAnalysisProducer;
import ustb.hyy.app.backend.domain.enums.EventType;
import ustb.hyy.app.backend.domain.enums.ObjectCategory;
import ustb.hyy.app.backend.domain.enums.TaskStatus;
import ustb.hyy.app.backend.dto.request.ProgressUpdateRequest;
import ustb.hyy.app.backend.dto.request.ResultSubmitRequest;
import ustb.hyy.app.backend.dto.request.TaskUploadRequest;
import ustb.hyy.app.backend.dto.response.TaskResponse;
import ustb.hyy.app.backend.dto.response.TaskResultResponse;
import ustb.hyy.app.backend.dto.response.TaskStatusResponse;
import ustb.hyy.app.backend.repository.*;
import ustb.hyy.app.backend.service.AnalysisTaskService;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析任务Service实现
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisTaskServiceImpl implements AnalysisTaskService {

    private final AnalysisTaskRepository taskRepository;
    private final TaskConfigRepository configRepository;
    private final DynamicMetricRepository metricRepository;
    private final AnomalyEventRepository eventRepository;
    private final TrackingObjectRepository trackingRepository;
    private final VideoAnalysisProducer analysisProducer;
    private final TaskProgressCache progressCache;

    @Value("${app.storage.video-path}")
    private String videoStoragePath;

    @Value("${app.ai-processor.callback-url}")
    private String aiCallbackUrl;

    @Value("${app.task.default-timeout-ratio}")
    private String defaultTimeoutRatio;

    @Value("${app.task.default-confidence-threshold}")
    private Double defaultConfidenceThreshold;

    @Value("${app.task.default-iou-threshold}")
    private Double defaultIouThreshold;

    @Override
    @Transactional
    public TaskResponse uploadTask(TaskUploadRequest request) {
        MultipartFile video = request.getVideo();

        // 1. 校验视频文件
        validateVideoFile(video);

        // 2. 保存视频文件
        String videoPath = saveVideoFile(video);

        // 3. 解析视频元数据（简化实现：假设30分钟，实际需要用FFmpeg解析）
        int videoDuration = parseVideoDuration(videoPath);

        // 4. 计算超时阈值
        String timeoutRatio = Optional.ofNullable(request.getTimeoutRatio()).orElse(defaultTimeoutRatio);
        int timeoutThreshold = calculateTimeoutThreshold(videoDuration, timeoutRatio);

        // 5. 创建任务
        AnalysisTask task = AnalysisTask.builder()
                .name(Optional.ofNullable(request.getName()).orElse(video.getOriginalFilename()))
                .videoPath(videoPath)
                .videoDuration(videoDuration)
                .status(TaskStatus.PENDING)
                .timeoutThreshold(timeoutThreshold)
                .isTimeout(false)
                .build();
        task = taskRepository.save(task);

        // 6. 创建任务配置
        TaskConfig config = TaskConfig.builder()
                .task(task)
                .timeoutRatio(timeoutRatio)
                .confidenceThreshold(Optional.ofNullable(request.getConfidenceThreshold()).orElse(defaultConfidenceThreshold))
                .iouThreshold(Optional.ofNullable(request.getIouThreshold()).orElse(defaultIouThreshold))
                .modelVersion("yolov11n")
                .build();
        configRepository.save(config);

        // 7. 返回响应（不再自动发送到消息队列，需要手动开始分析）
        return buildTaskResponse(task, config);
    }

    @Override
    @Transactional
    public void startAnalysis(Long taskId) {
        AnalysisTask task = findTaskById(taskId);

        // 检查任务状态，只有PENDING状态的任务可以开始分析
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new BusinessException(400, "任务状态不允许开始分析，当前状态: " + task.getStatus().name());
        }

        // 获取任务配置
        TaskConfig config = configRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务配置", taskId));

        // 发送MQ消息到AI处理模块
        VideoAnalysisMessage message = VideoAnalysisMessage.builder()
                .taskId(task.getId())
                .videoPath(task.getVideoPath())
                .videoDuration(task.getVideoDuration())
                .timeoutThreshold(task.getTimeoutThreshold())
                .callbackUrl(aiCallbackUrl)
                .config(VideoAnalysisMessage.TaskConfigData.builder()
                        .timeoutRatio(config.getTimeoutRatio())
                        .confidenceThreshold(config.getConfidenceThreshold())
                        .iouThreshold(config.getIouThreshold())
                        .modelVersion(config.getModelVersion())
                        .build())
                .build();
        analysisProducer.sendAnalysisTask(message);

        log.info("任务已发送到分析队列，taskId: {}", taskId);
    }

    @Override
    public TaskResponse getTask(Long taskId) {
        AnalysisTask task = findTaskById(taskId);
        TaskConfig config = configRepository.findByTaskId(taskId).orElse(null);
        return buildTaskResponse(task, config);
    }

    @Override
    public TaskStatusResponse getTaskStatus(Long taskId) {
        // 优先从Redis缓存获取实时进度
        TaskStatusResponse cachedProgress = progressCache.getProgress(taskId);
        if (cachedProgress != null) {
            return cachedProgress;
        }

        // 缓存不存在，从数据库查询
        AnalysisTask task = findTaskById(taskId);
        return TaskStatusResponse.builder()
                .taskId(task.getId())
                .status(task.getStatus().name())
                .isTimeout(task.getIsTimeout())
                .failureReason(task.getFailureReason())
                .build();
    }

    @Override
    @Transactional
    public void updateProgress(Long taskId, ProgressUpdateRequest request) {
        AnalysisTask task = findTaskById(taskId);

        // 更新任务状态
        TaskStatus newStatus = TaskStatus.valueOf(request.getStatus());
        task.setStatus(newStatus);

        // 更新时间戳
        if (newStatus == TaskStatus.PREPROCESSING && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        } else if (newStatus == TaskStatus.ANALYZING && task.getPreprocessingCompletedAt() == null) {
            task.setPreprocessingCompletedAt(LocalDateTime.now());
        } else if (newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.COMPLETED_TIMEOUT) {
            task.setCompletedAt(LocalDateTime.now());
        }

        // 更新超时状态
        if (Boolean.TRUE.equals(request.getIsTimeout())) {
            task.setIsTimeout(true);
        }

        // 更新失败原因
        if (newStatus == TaskStatus.FAILED) {
            task.setFailureReason(request.getFailureReason());
        }

        taskRepository.save(task);

        // 更新Redis缓存中的实时进度
        TaskStatusResponse statusResponse = TaskStatusResponse.builder()
                .taskId(taskId)
                .status(request.getStatus())
                .phase(request.getPhase())
                .progress(request.getProgress())
                .currentFrame(request.getCurrentFrame())
                .totalFrames(request.getTotalFrames())
                .preprocessingDuration(request.getPreprocessingDuration())
                .analyzingElapsedTime(request.getAnalyzingElapsedTime())
                .isTimeout(request.getIsTimeout())
                .timeoutWarning(request.getTimeoutWarning())
                .failureReason(request.getFailureReason())
                .build();
        progressCache.cacheProgress(taskId, statusResponse);
        progressCache.cacheStatus(taskId, request.getStatus());

        // 设置超时预警
        if (Boolean.TRUE.equals(request.getTimeoutWarning())) {
            progressCache.setTimeoutWarning(taskId);
        }

        log.info("任务进度已更新并缓存，taskId: {}, status: {}, progress: {}",
                taskId, newStatus, request.getProgress());
    }

    @Override
    @Transactional
    public void submitResult(Long taskId, ResultSubmitRequest request) {
        AnalysisTask task = findTaskById(taskId);

        // 1. 更新任务状态
        TaskStatus newStatus = TaskStatus.valueOf(request.getStatus());
        task.setStatus(newStatus);
        task.setCompletedAt(LocalDateTime.now());

        if (newStatus == TaskStatus.FAILED) {
            task.setFailureReason(request.getFailureReason());
        }
        taskRepository.save(task);

        // 2. 保存动态参数
        if (request.getDynamicMetrics() != null && !request.getDynamicMetrics().isEmpty()) {
            List<DynamicMetric> metrics = request.getDynamicMetrics().stream()
                    .map(data -> DynamicMetric.builder()
                            .taskId(taskId)
                            .frameNumber(data.getFrameNumber())
                            .timestamp(BigDecimal.valueOf(data.getTimestamp()))
                            .flickerFrequency(data.getFlickerFrequency() != null ?
                                    BigDecimal.valueOf(data.getFlickerFrequency()) : null)
                            .poolArea(data.getPoolArea())
                            .poolPerimeter(data.getPoolPerimeter() != null ?
                                    BigDecimal.valueOf(data.getPoolPerimeter()) : null)
                            .build())
                    .collect(Collectors.toList());
            metricRepository.saveAll(metrics);
        }

        // 3. 保存异常事件
        if (request.getAnomalyEvents() != null && !request.getAnomalyEvents().isEmpty()) {
            List<AnomalyEvent> events = request.getAnomalyEvents().stream()
                    .map(data -> AnomalyEvent.builder()
                            .taskId(taskId)
                            .eventType(EventType.valueOf(data.getEventType()))
                            .startFrame(data.getStartFrame())
                            .endFrame(data.getEndFrame())
                            .objectId(data.getObjectId())
                            .metadata(data.getMetadata() != null ? (Map<String, Object>) data.getMetadata() : null)
                            .build())
                    .collect(Collectors.toList());
            eventRepository.saveAll(events);
        }

        // 4. 保存追踪物体
        if (request.getTrackingObjects() != null && !request.getTrackingObjects().isEmpty()) {
            List<TrackingObject> objects = request.getTrackingObjects().stream()
                    .map(data -> TrackingObject.builder()
                            .taskId(taskId)
                            .objectId(data.getObjectId())
                            .category(ObjectCategory.valueOf(data.getCategory()))
                            .firstFrame(data.getFirstFrame())
                            .lastFrame(data.getLastFrame())
                            .trajectory(data.getTrajectory() != null ? (List<Map<String, Object>>) data.getTrajectory() : null)
                            .build())
                    .collect(Collectors.toList());
            trackingRepository.saveAll(objects);
        }

        // 任务完成，清除Redis进度缓存
        progressCache.deleteProgress(taskId);

        log.info("任务结果已提交，taskId: {}, status: {}", taskId, newStatus);
    }

    @Override
    public TaskResultResponse getTaskResult(Long taskId) {
        AnalysisTask task = findTaskById(taskId);

        // 检查任务是否完成
        if (task.getStatus() != TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED_TIMEOUT) {
            throw new BusinessException("任务尚未完成，无法获取结果");
        }

        // 查询动态参数
        List<DynamicMetric> metrics = metricRepository.findByTaskIdOrderByFrameNumberAsc(taskId);
        List<TaskResultResponse.DynamicMetricData> metricDataList = metrics.stream()
                .map(m -> TaskResultResponse.DynamicMetricData.builder()
                        .frameNumber(m.getFrameNumber())
                        .timestamp(m.getTimestamp() != null ? m.getTimestamp().doubleValue() : null)
                        .flickerFrequency(m.getFlickerFrequency() != null ? m.getFlickerFrequency().doubleValue() : null)
                        .poolArea(m.getPoolArea())
                        .poolPerimeter(m.getPoolPerimeter() != null ? m.getPoolPerimeter().doubleValue() : null)
                        .build())
                .collect(Collectors.toList());

        // 查询异常事件
        List<AnomalyEvent> events = eventRepository.findByTaskIdOrderByStartFrameAsc(taskId);
        List<TaskResultResponse.AnomalyEventData> eventDataList = events.stream()
                .map(e -> TaskResultResponse.AnomalyEventData.builder()
                        .eventId(e.getId())
                        .eventType(e.getEventType().name())
                        .startFrame(e.getStartFrame())
                        .endFrame(e.getEndFrame())
                        .objectId(e.getObjectId())
                        .metadata(e.getMetadata())
                        .build())
                .collect(Collectors.toList());

        // 查询追踪物体
        List<TrackingObject> objects = trackingRepository.findByTaskIdOrderByFirstFrameAsc(taskId);
        List<TaskResultResponse.TrackingObjectData> objectDataList = objects.stream()
                .map(o -> TaskResultResponse.TrackingObjectData.builder()
                        .trackingId(o.getId())
                        .objectId(o.getObjectId())
                        .category(o.getCategory().name())
                        .firstFrame(o.getFirstFrame())
                        .lastFrame(o.getLastFrame())
                        .trajectory(o.getTrajectory())
                        .build())
                .collect(Collectors.toList());

        // 统计事件类型
        Map<String, Long> eventStats = events.stream()
                .collect(Collectors.groupingBy(e -> e.getEventType().name(), Collectors.counting()));

        // 统计物体类别
        Map<String, Long> objectStats = objects.stream()
                .collect(Collectors.groupingBy(o -> o.getCategory().name(), Collectors.counting()));

        return TaskResultResponse.builder()
                .taskId(task.getId())
                .name(task.getName())
                .status(task.getStatus().name())
                .isTimeout(task.getIsTimeout())
                .dynamicMetrics(metricDataList)
                .anomalyEvents(eventDataList)
                .trackingObjects(objectDataList)
                .eventStatistics(eventStats)
                .objectStatistics(objectStats)
                .build();
    }

    @Override
    public PageResult<TaskResponse> listTasks(Pageable pageable) {
        Page<AnalysisTask> taskPage = taskRepository.findAll(pageable);
        List<TaskResponse> responses = taskPage.getContent().stream()
                .map(task -> {
                    TaskConfig config = configRepository.findByTaskId(task.getId()).orElse(null);
                    return buildTaskResponse(task, config);
                })
                .collect(Collectors.toList());
        return PageResult.of(taskPage, responses);
    }

    @Override
    public PageResult<TaskResponse> listTasksByStatus(String status, Pageable pageable) {
        TaskStatus taskStatus = TaskStatus.valueOf(status);
        Page<AnalysisTask> taskPage = taskRepository.findByStatus(taskStatus, pageable);
        List<TaskResponse> responses = taskPage.getContent().stream()
                .map(task -> {
                    TaskConfig config = configRepository.findByTaskId(task.getId()).orElse(null);
                    return buildTaskResponse(task, config);
                })
                .collect(Collectors.toList());
        return PageResult.of(taskPage, responses);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        AnalysisTask task = findTaskById(taskId);

        // 删除视频文件
        try {
            Files.deleteIfExists(Paths.get(task.getVideoPath()));
        } catch (IOException e) {
            log.warn("删除视频文件失败: {}", task.getVideoPath(), e);
        }

        // 删除任务（级联删除所有相关数据）
        taskRepository.delete(task);
        log.info("任务已删除，taskId: {}", taskId);
    }

    // ==================== 私有辅助方法 ====================

    private AnalysisTask findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务", taskId));
    }

    private void validateVideoFile(MultipartFile video) {
        if (video.isEmpty()) {
            throw new BusinessException(400, "视频文件不能为空");
        }

        String filename = video.getOriginalFilename();
        if (filename == null || !isValidVideoFormat(filename)) {
            throw new BusinessException(1001, "不支持的视频格式，仅支持mp4/avi/mov/mkv");
        }

        if (video.getSize() > 2L * 1024 * 1024 * 1024) {
            throw new BusinessException(400, "视频文件不能超过2GB");
        }
    }

    private boolean isValidVideoFormat(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return Arrays.asList("mp4", "avi", "mov", "mkv").contains(extension);
    }

    private String saveVideoFile(MultipartFile video) {
        try {
            Path storagePath = Paths.get(videoStoragePath);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            String filename = System.currentTimeMillis() + "_" + video.getOriginalFilename();
            Path filePath = storagePath.resolve(filename);
            Files.copy(video.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        } catch (IOException e) {
            log.error("视频文件保存失败", e);
            throw new BusinessException("视频文件保存失败", e);
        }
    }

    private int parseVideoDuration(String videoPath) {
        return VideoUtils.parseVideoDuration(videoPath);
    }

    private int calculateTimeoutThreshold(int videoDuration, String timeoutRatio) {
        String[] parts = timeoutRatio.split(":");
        if (parts.length != 2) {
            throw new BusinessException(400, "超时比例格式不正确");
        }

        try {
            int numerator = Integer.parseInt(parts[0]);
            int denominator = Integer.parseInt(parts[1]);
            return videoDuration * denominator / numerator;
        } catch (NumberFormatException e) {
            throw new BusinessException(400, "超时比例格式不正确", e);
        }
    }

    private TaskResponse buildTaskResponse(AnalysisTask task, TaskConfig config) {
        TaskResponse.TaskConfigData configData = null;
        if (config != null) {
            configData = TaskResponse.TaskConfigData.builder()
                    .timeoutRatio(config.getTimeoutRatio())
                    .confidenceThreshold(config.getConfidenceThreshold())
                    .iouThreshold(config.getIouThreshold())
                    .modelVersion(config.getModelVersion())
                    .build();
        }

        return TaskResponse.builder()
                .taskId(String.valueOf(task.getId()))
                .name(task.getName())
                .videoDuration(task.getVideoDuration())
                .status(task.getStatus().name())
                .timeoutThreshold(task.getTimeoutThreshold())
                .isTimeout(task.getIsTimeout())
                .config(configData)
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .preprocessingCompletedAt(task.getPreprocessingCompletedAt())
                .completedAt(task.getCompletedAt())
                .failureReason(task.getFailureReason())
                .build();
    }
}
