package ustb.hyy.app.backend.mq.message;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频分析任务消息
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoAnalysisMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 视频文件路径
     */
    private String videoPath;

    private Integer videoDuration;

    private Integer timeoutThreshold;

    /**
     * 回调URL（后端接口）
     */
    private String callbackUrl;

    /**
     * 任务配置
     */
    private TaskConfigData config;

    /**
     * 任务配置数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskConfigData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * 超时比例
         */
        private String timeoutRatio;

        /**
         * 模型版本
         */
        private String modelVersion;

        /**
         * 是否启用预处理
         */
        private Boolean enablePreprocessing;

        /**
         * 预处理强度
         */
        private String preprocessingStrength;

        /**
         * 是否启用熔池增强
         */
        private Boolean preprocessingEnhancePool;

        /**
         * 是否启用追踪轨迹合并
         */
        private Boolean enableTrackingMerge;

        /**
         * 追踪合并策略
         */
        private String trackingMergeStrategy;

        /**
         * 视频帧率（由FFmpeg解析得到）
         */
        private Double frameRate;
    }
}
