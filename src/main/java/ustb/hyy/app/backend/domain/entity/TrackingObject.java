package ustb.hyy.app.backend.domain.entity;

import org.hibernate.annotations.Type;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ustb.hyy.app.backend.domain.enums.ObjectCategory;

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
     * 轨迹数据（JSON数组，存储物体在每帧的位置和置信度）
     * 格式：[
     *   {"frame": 100, "bbox": [x1, y1, x2, y2], "confidence": 0.95},
     *   {"frame": 101, "bbox": [x1, y1, x2, y2], "confidence": 0.92},
     *   ...
     * ]
     * 说明：
     * - frame: 帧号（物体在该帧出现）
     * - bbox: 边界框坐标 [x1, y1, x2, y2]（左上角和右下角坐标）
     * - confidence: 检测置信度（0.0-1.0）
     * - 数组长度 = 物体实际出现的帧数（非视频总帧数）
     * - 如果物体间断出现，只记录出现的帧
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Object trajectory;  // 使用 Object 类型以支持灵活的 JSON 格式
}
