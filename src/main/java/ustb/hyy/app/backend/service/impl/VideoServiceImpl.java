package ustb.hyy.app.backend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ustb.hyy.app.backend.common.exception.BusinessException;
import ustb.hyy.app.backend.common.exception.ResourceNotFoundException;
import ustb.hyy.app.backend.domain.entity.AnalysisTask;
import ustb.hyy.app.backend.repository.AnalysisTaskRepository;
import ustb.hyy.app.backend.service.VideoService;

/**
 * 视频服务实现
 *
 * @author 侯阳洋
 * @since 2025-10-04
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private final AnalysisTaskRepository taskRepository;

    @Override
    public Resource getVideoResource(Long taskId, String type) {
        String videoPath = getVideoPath(taskId, type);
        return loadResource(videoPath);
    }

    @Override
    public Resource getVideoResourceRange(Long taskId, String type, List<HttpRange> ranges) {
        // 对于范围请求，返回同样的资源，Spring会自动处理范围
        return getVideoResource(taskId, type);
    }

    @Override
    public VideoMetadata getVideoMetadata(Long taskId, String type) {
        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在：" + taskId));

        String videoPath = getVideoPath(taskId, type);
        Path path = Paths.get(videoPath);

        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("视频文件不存在");
        }

        try {
            long fileSize = Files.size(path);
            String filename = path.getFileName().toString();
            String contentType = Files.probeContentType(path);

            // 如果系统无法识别Content-Type，根据文件扩展名设置
            if (contentType == null) {
                String lowerFilename = filename.toLowerCase();
                if (lowerFilename.endsWith(".mp4")) {
                    contentType = "video/mp4";
                } else if (lowerFilename.endsWith(".mkv")) {
                    contentType = "video/x-matroska";
                } else if (lowerFilename.endsWith(".webm")) {
                    contentType = "video/webm";
                } else if (lowerFilename.endsWith(".avi")) {
                    contentType = "video/x-msvideo";
                } else if (lowerFilename.endsWith(".mov")) {
                    contentType = "video/quicktime";
                } else {
                    contentType = "video/mp4"; // 最终默认值
                }
            }

            return new VideoMetadata(
                    filename,
                    contentType,
                    fileSize,
                    task.getVideoDuration()
            );
        } catch (IOException e) {
            log.error("获取视频元数据失败", e);
            throw new BusinessException("获取视频元数据失败");
        }
    }

    /**
     * 获取视频路径
     */
    private String getVideoPath(Long taskId, String type) {
        AnalysisTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("任务不存在：" + taskId));

        String path;
        if ("original".equalsIgnoreCase(type)) {
            path = task.getVideoPath();
        } else if ("result".equalsIgnoreCase(type)) {
            path = task.getResultVideoPath();
            if (path == null || path.isEmpty()) {
                throw new ResourceNotFoundException("结果视频尚未生成");
            }
        } else if ("preprocessed".equalsIgnoreCase(type)) {
            path = task.getPreprocessedVideoPath();
            if (path == null || path.isEmpty()) {
                throw new ResourceNotFoundException("预处理视频尚未生成");
            }
        } else {
            throw new BusinessException("不支持的视频类型：" + type);
        }

        // 数据库中存储的是相对于 codes/ 目录的路径（例如：storage/videos/xxx.mp4）
        // backend 的工作目录是 codes/backend/，需要转换为相对于 backend 的路径
        return "../" + path;
    }

    /**
     * 加载资源
     */
    private Resource loadResource(String filePath) {
        try {
            Path path = Paths.get(filePath).normalize();
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("视频文件不存在或不可读：" + filePath);
            }
        } catch (IOException e) {
            log.error("加载视频资源失败：{}", filePath, e);
            throw new BusinessException("加载视频资源失败");
        }
    }
}
