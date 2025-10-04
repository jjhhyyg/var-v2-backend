package ustb.hyy.app.backend.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;

/**
 * 视频服务接口
 *
 * @author 侯阳洋
 * @since 2025-10-04
 */
public interface VideoService {

    /**
     * 获取视频资源
     *
     * @param taskId 任务ID
     * @param type 视频类型（original: 原始视频, result: 结果视频）
     * @return 视频资源
     */
    Resource getVideoResource(Long taskId, String type);

    /**
     * 获取视频部分内容（支持断点续传）
     *
     * @param taskId 任务ID
     * @param type 视频类型
     * @param ranges HTTP Range头
     * @return 视频资源
     */
    Resource getVideoResourceRange(Long taskId, String type, List<HttpRange> ranges);

    /**
     * 获取视频元数据
     *
     * @param taskId 任务ID
     * @param type 视频类型
     * @return 视频元数据
     */
    VideoMetadata getVideoMetadata(Long taskId, String type);

    /**
     * 视频元数据类
     */
    record VideoMetadata(
            String filename,
            String contentType,
            long fileSize,
            Integer duration
    ) {}
}
