package ustb.hyy.app.backend.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务响应DTO
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 视频时长（秒）
     */
    private Integer videoDuration;

    /**
     * 结果视频路径（带标注的视频）
     */
    private String resultVideoPath;

    /**
     * 预处理后的视频路径
     */
    private String preprocessedVideoPath;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 超时阈值（秒）
     */
    private Integer timeoutThreshold;

    /**
     * 是否超时
     */
    private Boolean isTimeout;

    /**
     * 任务配置
     */
    private TaskConfigData config;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startedAt;

    /**
     * 预处理完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime preprocessingCompletedAt;

    /**
     * 任务完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 任务配置数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskConfigData {
        private String timeoutRatio;
        private String modelVersion;
        private Boolean enablePreprocessing;
        private String preprocessingStrength;
        private Boolean preprocessingEnhancePool;
        private Boolean enableTrackingMerge;
        private String trackingMergeStrategy;
        private Double frameRate;
    }
}
