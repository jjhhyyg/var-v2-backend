package ustb.hyy.app.backend.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ustb.hyy.app.backend.service.VideoService;

/**
 * 视频流传输控制器
 *
 * @author 侯阳洋
 * @since 2025-10-04
 */
@Tag(name = "视频管理", description = "视频流传输和元数据查询接口")
@Slf4j
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    /**
     * 获取视频流（支持范围请求）
     */
    @Operation(summary = "获取视频流", description = "支持HTTP Range请求，实现视频流式传输和断点续传")
    @GetMapping("/{taskId:[0-9]+}/{type}")
    public ResponseEntity<ResourceRegion> streamVideo(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "视频类型（original/result）") @PathVariable String type,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) throws IOException {

        log.info("视频流请求 - taskId: {}, type: {}, range: {}", taskId, type, rangeHeader);

        // 获取视频元数据
        VideoService.VideoMetadata metadata = videoService.getVideoMetadata(taskId, type);
        Resource resource = videoService.getVideoResource(taskId, type);

        // 准备响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(metadata.contentType()));
        headers.setCacheControl(CacheControl.maxAge(3600, java.util.concurrent.TimeUnit.SECONDS));
        headers.set(HttpHeaders.ACCEPT_RANGES, "bytes");
        // 使用RFC 5987标准编码文件名，支持UTF-8中文
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(metadata.filename(), StandardCharsets.UTF_8)
                        .build()
        );

        // 处理范围请求
        if (rangeHeader != null && !rangeHeader.isEmpty()) {
            return handleRangeRequest(resource, metadata, rangeHeader, headers);
        }

        // 完整响应 - 使用ResourceRegion包装整个资源
        ResourceRegion region = new ResourceRegion(resource, 0, metadata.fileSize());
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(region);
    }

    /**
     * 获取视频元数据
     */
    @Operation(summary = "获取视频元数据", description = "获取视频文件信息，包括文件名、大小、时长等")
    @GetMapping("/{taskId:[0-9]+}/{type}/metadata")
    public ResponseEntity<VideoService.VideoMetadata> getVideoMetadata(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Parameter(description = "视频类型（original/result）") @PathVariable String type) {

        log.info("获取视频元数据 - taskId: {}, type: {}", taskId, type);
        VideoService.VideoMetadata metadata = videoService.getVideoMetadata(taskId, type);
        return ResponseEntity.ok(metadata);
    }

    /**
     * 处理范围请求
     */
    private ResponseEntity<ResourceRegion> handleRangeRequest(
            Resource resource,
            VideoService.VideoMetadata metadata,
            String rangeHeader,
            HttpHeaders headers) throws IOException {

        long fileSize = metadata.fileSize();
        List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);

        if (ranges.isEmpty()) {
            // 无效的范围，返回完整内容的第一个字节范围
            ResourceRegion region = new ResourceRegion(resource, 0, fileSize);
            return ResponseEntity.ok().headers(headers).body(region);
        }

        // 只处理第一个范围（简化实现）
        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(fileSize);
        long end = range.getRangeEnd(fileSize);
        long contentLength = end - start + 1;

        log.debug("范围请求 - start: {}, end: {}, length: {}", start, end, contentLength);

        // 创建ResourceRegion，Spring会自动设置正确的响应头
        ResourceRegion region = new ResourceRegion(resource, start, contentLength);

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .headers(headers)
                .body(region);
    }
}
