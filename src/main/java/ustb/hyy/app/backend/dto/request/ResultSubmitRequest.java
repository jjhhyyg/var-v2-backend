package ustb.hyy.app.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 结果提交请求（AI模块回调）
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
public class ResultSubmitRequest {

    /**
     * 任务状态
     */
    @NotBlank(message = "任务状态不能为空")
    @Pattern(regexp = "^(COMPLETED|COMPLETED_TIMEOUT|FAILED)$", message = "任务状态无效")
    private String status;

    /**
     * 失败原因（仅status=FAILED时）
     */
    private String failureReason;

    /**
     * 动态参数列表
     */
    @Valid
    private List<DynamicMetricData> dynamicMetrics;

    /**
     * 异常事件列表
     */
    @Valid
    private List<AnomalyEventData> anomalyEvents;

    /**
     * 追踪物体列表
     */
    @Valid
    private List<TrackingObjectData> trackingObjects;

    /**
     * 动态参数数据
     */
    @Data
    public static class DynamicMetricData {
        @NotNull(message = "帧号不能为空")
        private Integer frameNumber;

        @NotNull(message = "时间戳不能为空")
        private Double timestamp;

        private Double flickerFrequency;
        private Integer poolArea;
        private Double poolPerimeter;
    }

    /**
     * 异常事件数据
     */
    @Data
    public static class AnomalyEventData {
        @NotBlank(message = "事件类型不能为空")
        private String eventType;

        @NotNull(message = "起始帧号不能为空")
        private Integer startFrame;

        @NotNull(message = "结束帧号不能为空")
        private Integer endFrame;

        private Integer objectId;
        private Object metadata; // JSON对象
    }

    /**
     * 追踪物体数据
     */
    @Data
    public static class TrackingObjectData {
        @NotNull(message = "物体ID不能为空")
        private Integer objectId;

        @NotBlank(message = "物体类别不能为空")
        private String category;

        @NotNull(message = "首次出现帧号不能为空")
        private Integer firstFrame;

        @NotNull(message = "最后出现帧号不能为空")
        private Integer lastFrame;

        private Object trajectory; // JSON数组
    }
}
