package ustb.hyy.app.backend.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import ustb.hyy.app.backend.domain.enums.ObjectCategory;

import java.util.List;
import java.util.Map;

/**
 * 追踪物体实体（ByteTrack追踪结果）
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Entity
@Table(name = "tracking_objects", indexes = {
        @Index(name = "idx_task_object", columnList = "task_id,object_id"),
        @Index(name = "idx_task_category", columnList = "task_id,category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackingObject extends BaseEntity {

    /**
     * 关联的任务ID
     */
    @Column(nullable = false)
    private Long taskId;

    /**
     * ByteTrack追踪物体ID
     */
    @Column(nullable = false)
    private Integer objectId;

    /**
     * 物体类别
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ObjectCategory category;

    /**
     * 首次出现帧号
     */
    @Column(nullable = false)
    private Integer firstFrame;

    /**
     * 最后出现帧号
     */
    @Column(nullable = false)
    private Integer lastFrame;

    /**
     * 轨迹数据（JSON数组，存储每一帧的位置、边界框等信息）
     * 示例：[{"frame": 100, "bbox": [x, y, w, h], "confidence": 0.85}, ...]
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private List<Map<String, Object>> trajectory;
}
