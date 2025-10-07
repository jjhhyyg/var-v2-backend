package ustb.hyy.app.backend.service.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ustb.hyy.app.backend.cache.TaskProgressCache;
import ustb.hyy.app.backend.common.exception.BusinessException;
import ustb.hyy.app.backend.common.exception.ResourceNotFoundException;
import ustb.hyy.app.backend.common.response.PageResult;
import ustb.hyy.app.backend.common.util.VideoUtils;
import ustb.hyy.app.backend.domain.entity.AnalysisTask;
import ustb.hyy.app.backend.domain.entity.AnomalyEvent;
import ustb.hyy.app.backend.domain.entity.DynamicMetric;
import ustb.hyy.app.backend.domain.entity.TaskConfig;
import ustb.hyy.app.backend.domain.entity.TrackingObject;
import ustb.hyy.app.backend.domain.enums.EventType;
import ustb.hyy.app.backend.domain.enums.ObjectCategory;
import ustb.hyy.app.backend.domain.enums.TaskStatus;
import ustb.hyy.app.backend.dto.request.ProgressUpdateRequest;
import ustb.hyy.app.backend.dto.request.ResultSubmitRequest;
import ustb.hyy.app.backend.dto.request.TaskUploadRequest;
import ustb.hyy.app.backend.dto.response.TaskResponse;
import ustb.hyy.app.backend.dto.response.TaskResultResponse;
import ustb.hyy.app.backend.dto.response.TaskStatusResponse;
import ustb.hyy.app.backend.mq.message.VideoAnalysisMessage;
import ustb.hyy.app.backend.mq.producer.VideoAnalysisProducer;
import ustb.hyy.app.backend.repository.AnalysisTaskRepository;
import ustb.hyy.app.backend.repository.AnomalyEventRepository;
import ustb.hyy.app.backend.repository.DynamicMetricRepository;
import ustb.hyy.app.backend.repository.TaskConfigRepository;
import ustb.hyy.app.backend.repository.TrackingObjectRepository;
import ustb.hyy.app.backend.service.AnalysisTaskService;

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
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${app.storage.video-path}")
    private String videoStoragePath;

    @Value("${app.ai-processor.callback-url}")
    private String aiCallbackUrl;

    @Value("${app.task.default-timeout-ratio}")
    private String defaultTimeoutRatio;

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
                .modelVersion("yolov11n")
                .enablePreprocessing(Optional.ofNullable(request.getEnablePreprocessing()).orElse(false))
                .preprocessingStrength(Optional.ofNullable(request.getPreprocessingStrength()).orElse("moderate"))
                .preprocessingEnhancePool(Optional.ofNullable(request.getPreprocessingEnhancePool()).orElse(true))
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
                        .modelVersion(config.getModelVersion())
                        .enablePreprocessing(config.getEnablePreprocessing())
                        .preprocessingStrength(config.getPreprocessingStrength())
                        .preprocessingEnhancePool(config.getPreprocessingEnhancePool())
                        .build())
                .build();
        analysisProducer.sendAnalysisTask(message);

        log.info("任务已发送到分析队列，taskId: {}", taskId);
    }

    @Override
    @Transactional
    public void reanalyzeTask(Long taskId) {
        AnalysisTask task = findTaskById(taskId);

        // 检查任务状态，只允许重新分析已完成、超时完成或失败的任务
        if (task.getStatus() != TaskStatus.COMPLETED && 
            task.getStatus() != TaskStatus.COMPLETED_TIMEOUT && 
            task.getStatus() != TaskStatus.FAILED) {
            throw new BusinessException(400, "只能重新分析已完成或失败的任务，当前状态: " + task.getStatus().name());
        }

        log.info("开始重新分析任务，taskId: {}, 当前状态: {}", taskId, task.getStatus());

        // 1. 清除旧的分析结果
        log.info("清除任务 {} 的旧分析数据", taskId);
        
        // 删除动态参数
        metricRepository.deleteByTaskId(taskId);
        
        // 删除异常事件
        eventRepository.deleteByTaskId(taskId);
        
        // 删除追踪物体
        trackingRepository.deleteByTaskId(taskId);
        
        // 2. 重置任务状态和时间戳
        task.setStatus(TaskStatus.PENDING);
        task.setIsTimeout(false);
        task.setFailureReason(null);
        task.setStartedAt(null);
        task.setPreprocessingCompletedAt(null);
        task.setCompletedAt(null);
        task.setResultVideoPath(null);
        taskRepository.save(task);

        // 3. 清除Redis缓存的进度信息
        progressCache.deleteProgress(taskId);

        log.info("任务 {} 状态已重置，旧数据已清除", taskId);

        // 4. 重新发送到MQ队列
        TaskConfig config = configRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务配置", taskId));

        VideoAnalysisMessage message = VideoAnalysisMessage.builder()
                .taskId(task.getId())
                .videoPath(task.getVideoPath())
                .videoDuration(task.getVideoDuration())
                .timeoutThreshold(task.getTimeoutThreshold())
                .callbackUrl(aiCallbackUrl)
                .config(VideoAnalysisMessage.TaskConfigData.builder()
                        .timeoutRatio(config.getTimeoutRatio())
                        .modelVersion(config.getModelVersion())
                        .enablePreprocessing(config.getEnablePreprocessing())
                        .preprocessingStrength(config.getPreprocessingStrength())
                        .preprocessingEnhancePool(config.getPreprocessingEnhancePool())
                        .build())
                .build();
        analysisProducer.sendAnalysisTask(message);

        log.info("任务 {} 已重新发送到分析队列", taskId);
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
            
            // 发送 WebSocket 通知：任务开始分析
            taskRepository.saveAndFlush(task);
            messagingTemplate.convertAndSend(
                "/topic/task/" + taskId + "/status",
                Map.of(
                    "status", "ANALYZING",
                    "phase", "分析中",
                    "timestamp", System.currentTimeMillis()
                )
            );
            messagingTemplate.convertAndSend("/topic/task/" + taskId, Map.of(
                "type", "STATUS_CHANGE",
                "status", "ANALYZING",
                "message", "任务开始分析"
            ));
            log.info("Task {}: Sent WebSocket notification for ANALYZING status", taskId);
        } else if ((newStatus == TaskStatus.COMPLETED || newStatus == TaskStatus.COMPLETED_TIMEOUT)
                && task.getCompletedAt() == null) {
            // 只在第一次完成时设置完成时间，避免生成结果视频时重复更新
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

        // 通过WebSocket推送任务状态更新
        try {
            // 推送到特定任务订阅者
            messagingTemplate.convertAndSend("/topic/tasks/" + taskId + "/status", statusResponse);
            // 推送到任务列表订阅者（简化版，只发送taskId和status）
            Map<String, Object> listUpdate = Map.of(
                "taskId", taskId,
                "status", request.getStatus(),
                "progress", Optional.ofNullable(request.getProgress()).orElse(0.0)
            );
            messagingTemplate.convertAndSend("/topic/tasks/updates", listUpdate);
            log.debug("WebSocket消息已推送，taskId: {}", taskId);
        } catch (Exception e) {
            log.error("WebSocket消息推送失败，taskId: {}", taskId, e);
            // 不影响主流程，仅记录错误
        }

        log.info("任务进度已更新并缓存，taskId: {}, status: {}, progress: {}",
                taskId, newStatus, request.getProgress());
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void submitResult(Long taskId, ResultSubmitRequest request) {
        AnalysisTask task = findTaskById(taskId);

        // 1. 更新任务状态和全局分析结果
        TaskStatus newStatus = TaskStatus.valueOf(request.getStatus());
        task.setStatus(newStatus);
        task.setCompletedAt(LocalDateTime.now());

        if (newStatus == TaskStatus.FAILED) {
            task.setFailureReason(request.getFailureReason());
        }

        // 保存全局频率分析结果
        if (request.getGlobalAnalysis() != null) {
            task.setGlobalAnalysis(request.getGlobalAnalysis());
        }

        taskRepository.save(task);

        // 2. 保存动态参数
        if (request.getDynamicMetrics() != null && !request.getDynamicMetrics().isEmpty()) {
            List<DynamicMetric> metrics = request.getDynamicMetrics().stream()
                    .map(data -> DynamicMetric.builder()
                            .taskId(taskId)
                            .frameNumber(data.getFrameNumber())
                            .timestamp(BigDecimal.valueOf(data.getTimestamp()))
                            .brightness(data.getBrightness() != null ?
                                    BigDecimal.valueOf(data.getBrightness()) : null)
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

        // 4. 保存追踪物体（使用 upsert 逻辑）
        if (request.getTrackingObjects() != null && !request.getTrackingObjects().isEmpty()) {
            for (ResultSubmitRequest.TrackingObjectData data : request.getTrackingObjects()) {
                // 查询是否已存在相同的 objectId
                Optional<TrackingObject> existingOpt = trackingRepository.findByTaskIdAndObjectId(taskId, data.getObjectId());
                
                if (existingOpt.isPresent()) {
                    // 已存在：更新记录（合并数据）
                    TrackingObject existing = existingOpt.get();
                    
                    // 取最小的 firstFrame 和最大的 lastFrame
                    existing.setFirstFrame(Math.min(existing.getFirstFrame(), data.getFirstFrame()));
                    existing.setLastFrame(Math.max(existing.getLastFrame(), data.getLastFrame()));
                    
                    // 合并 trajectory（如果新数据有轨迹）
                    if (data.getTrajectory() != null) {
                        // trajectory 现在是 List<Map> 格式：[{frame: 100, bbox: [...], confidence: 0.95}, ...]
                        if (existing.getTrajectory() != null) {
                            // 现有轨迹存在，需要合并
                            List<?> existingList = (List<?>) existing.getTrajectory();
                            List<?> newList = (List<?>) data.getTrajectory();
                            
                            List<Object> mergedTrajectory = new ArrayList<>(existingList);
                            mergedTrajectory.addAll(newList);
                            existing.setTrajectory(mergedTrajectory);
                        } else {
                            // 现有轨迹为空，直接使用新轨迹
                            existing.setTrajectory(data.getTrajectory());
                        }
                    }
                    
                    trackingRepository.save(existing);
                    log.debug("更新追踪物体: taskId={}, objectId={}, firstFrame={}, lastFrame={}", 
                            taskId, data.getObjectId(), existing.getFirstFrame(), existing.getLastFrame());
                } else {
                    // 不存在：创建新记录
                    TrackingObject newObject = TrackingObject.builder()
                            .taskId(taskId)
                            .objectId(data.getObjectId())
                            .category(ObjectCategory.valueOf(data.getCategory()))
                            .firstFrame(data.getFirstFrame())
                            .lastFrame(data.getLastFrame())
                            .trajectory(data.getTrajectory())
                            .build();
                    trackingRepository.save(newObject);
                    log.debug("创建追踪物体: taskId={}, objectId={}, firstFrame={}, lastFrame={}", 
                            taskId, data.getObjectId(), data.getFirstFrame(), data.getLastFrame());
                }
            }
        }

        // 任务完成，清除Redis进度缓存
        progressCache.deleteProgress(taskId);

        // 通过WebSocket推送任务完成状态更新
        try {
            // 推送到特定任务订阅者
            TaskStatusResponse statusResponse = TaskStatusResponse.builder()
                    .taskId(taskId)
                    .status(newStatus.name())
                    .isTimeout(task.getIsTimeout())
                    .failureReason(task.getFailureReason())
                    .build();
            messagingTemplate.convertAndSend("/topic/tasks/" + taskId + "/status", statusResponse);
            
            // 推送到任务列表订阅者（简化版，只发送taskId和status）
            Map<String, Object> listUpdate = Map.of(
                "taskId", taskId,
                "status", newStatus.name(),
                "progress", 1.0
            );
            messagingTemplate.convertAndSend("/topic/tasks/updates", listUpdate);
            log.debug("WebSocket消息已推送（任务完成），taskId: {}, status: {}", taskId, newStatus);
        } catch (Exception e) {
            log.error("WebSocket消息推送失败（任务完成），taskId: {}", taskId, e);
            // 不影响主流程，仅记录错误
        }

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
                        .brightness(m.getBrightness() != null ? m.getBrightness().doubleValue() : null)
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
                .globalAnalysis(task.getGlobalAnalysis())
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
            if (task.getResultVideoPath() != null) {
                Files.deleteIfExists(Paths.get(task.getResultVideoPath()));
            }
        } catch (IOException e) {
            log.warn("删除视频文件失败: {}", task.getVideoPath(), e);
        }

        // 删除任务（级联删除所有相关数据）
        taskRepository.delete(task);
        log.info("任务已删除，taskId: {}", taskId);
    }

    @Override
    @Transactional
    public void updateResultVideoPath(Long taskId, String resultVideoPath) {
        AnalysisTask task = findTaskById(taskId);
        task.setResultVideoPath(resultVideoPath);
        taskRepository.save(task);
        log.info("更新任务结果视频路径，taskId: {}, resultVideoPath: {}", taskId, resultVideoPath);

        // 清除Redis中的进度缓存，避免前端一直显示"生成结果视频"进度
        progressCache.deleteProgress(taskId);
        log.debug("已清除任务进度缓存，taskId: {}", taskId);

        // 通过WebSocket推送更新，通知前端重新加载任务信息
        try {
            TaskConfig config = configRepository.findByTaskId(taskId).orElse(null);
            TaskResponse response = buildTaskResponse(task, config);
            messagingTemplate.convertAndSend("/topic/tasks/" + taskId + "/update", response);
            log.debug("WebSocket消息已推送（结果视频路径更新），taskId: {}", taskId);
        } catch (Exception e) {
            log.error("WebSocket消息推送失败（结果视频路径更新），taskId: {}", taskId, e);
        }
    }

    @Override
    @Transactional
    public void updatePreprocessedVideoPath(Long taskId, String preprocessedVideoPath) {
        AnalysisTask task = findTaskById(taskId);
        task.setPreprocessedVideoPath(preprocessedVideoPath);
        taskRepository.save(task);
        log.info("更新任务预处理视频路径，taskId: {}, preprocessedVideoPath: {}", taskId, preprocessedVideoPath);

        // 通过WebSocket推送更新，通知前端重新加载任务信息
        try {
            TaskConfig config = configRepository.findByTaskId(taskId).orElse(null);
            TaskResponse response = buildTaskResponse(task, config);
            messagingTemplate.convertAndSend("/topic/tasks/" + taskId + "/update", response);
            log.debug("WebSocket消息已推送（预处理视频路径更新），taskId: {}", taskId);
        } catch (Exception e) {
            log.error("WebSocket消息推送失败（预处理视频路径更新），taskId: {}", taskId, e);
        }
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

            // 直接使用原始文件名，不添加时间戳
            String filename = video.getOriginalFilename();
            Path filePath = storagePath.resolve(filename);
            Files.copy(video.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对于 codes/ 目录的路径
            // videoStoragePath 配置为 ../storage/videos（相对于 codes/backend）
            // 需要转换为相对于 codes/ 的路径：storage/videos/xxx.mp4
            String absolutePath = filePath.toAbsolutePath().normalize().toString();
            Path codesPath = Paths.get(System.getProperty("user.dir")).getParent(); // codes/backend -> codes
            String relativePath = codesPath.toAbsolutePath().normalize().relativize(Paths.get(absolutePath)).toString();

            // 统一使用正斜杠（跨平台兼容）
            return relativePath.replace("\\", "/");
        } catch (IOException e) {
            log.error("视频文件保存失败", e);
            throw new BusinessException("视频文件保存失败", e);
        }
    }

    private int parseVideoDuration(String videoPath) {
        // videoPath 是相对于 codes/ 的路径（如 storage/videos/xxx.mp4）
        // 需要转换为相对于 codes/backend/ 的路径（如 ../storage/videos/xxx.mp4）
        String actualPath = "../" + videoPath;
        return VideoUtils.parseVideoDuration(actualPath);
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
                    .modelVersion(config.getModelVersion())
                    .enablePreprocessing(config.getEnablePreprocessing())
                    .preprocessingStrength(config.getPreprocessingStrength())
                    .preprocessingEnhancePool(config.getPreprocessingEnhancePool())
                    .build();
        }

        return TaskResponse.builder()
                .taskId(String.valueOf(task.getId()))
                .name(task.getName())
                .videoDuration(task.getVideoDuration())
                .resultVideoPath(task.getResultVideoPath())
                .preprocessedVideoPath(task.getPreprocessedVideoPath())
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
