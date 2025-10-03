package ustb.hyy.app.backend.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import ustb.hyy.app.backend.domain.enums.EventType;

import java.util.Map;

/**
 * 异常事件实体
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Entity
@Table(name = "anomaly_events", indexes = {
        @Index(name = "idx_task_type", columnList = "task_id,event_type"),
        @Index(name = "idx_task_start_frame", columnList = "task_id,start_frame")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnomalyEvent extends BaseEntity {

    /**
     * 关联的任务ID
     */
    @Column(nullable = false)
    private Long taskId;

    /**
     * 事件类型
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventType eventType;

    /**
     * 起始帧号
     */
    @Column(nullable = false)
    private Integer startFrame;

    /**
     * 结束帧号（如果是瞬时事件，与startFrame相同）
     */
    @Column(nullable = false)
    private Integer endFrame;

    /**
     * ByteTrack追踪物体ID（如果事件与追踪物体相关）
     */
    @Column
    private Integer objectId;

    /**
     * 元数据（JSON格式，存储位置、轨迹、置信度等信息）
     * 示例：{"position": {"x": 100, "y": 200}, "confidence": 0.85, "dropLocation": "pool"}
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}
