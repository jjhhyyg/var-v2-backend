package ustb.hyy.app.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 任务结果响应DTO
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResultResponse {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 是否超时
     */
    private Boolean isTimeout;

    /**
     * 动态参数列表
     */
    private List<DynamicMetricData> dynamicMetrics;

    /**
     * 异常事件列表
     */
    private List<AnomalyEventData> anomalyEvents;

    /**
     * 追踪物体列表
     */
    private List<TrackingObjectData> trackingObjects;

    /**
     * 事件统计
     */
    private Map<String, Long> eventStatistics;

    /**
     * 物体统计
     */
    private Map<String, Long> objectStatistics;

    /**
     * 动态参数数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DynamicMetricData {
        private Integer frameNumber;
        private Double timestamp;
        private Double flickerFrequency;
        private Integer poolArea;
        private Double poolPerimeter;
    }

    /**
     * 异常事件数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyEventData {
        private Long eventId;
        private String eventType;
        private Integer startFrame;
        private Integer endFrame;
        private Integer objectId;
        private Map<String, Object> metadata;
    }

    /**
     * 追踪物体数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingObjectData {
        private Long trackingId;
        private Integer objectId;
        private String category;
        private Integer firstFrame;
        private Integer lastFrame;
        private List<Map<String, Object>> trajectory;
    }
}
