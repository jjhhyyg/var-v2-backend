package ustb.hyy.app.backend.mq.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

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
         * 置信度阈值
         */
        private Double confidenceThreshold;

        /**
         * IoU阈值
         */
        private Double iouThreshold;

        /**
         * 模型版本
         */
        private String modelVersion;
    }
}
