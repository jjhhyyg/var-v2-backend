package ustb.hyy.app.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 动态参数实体（时序数据）
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Entity
@Table(name = "dynamic_metrics", indexes = {
        @Index(name = "idx_task_frame", columnList = "task_id,frame_number"),
        @Index(name = "idx_task_timestamp", columnList = "task_id,timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DynamicMetric extends BaseEntity {

    /**
     * 关联的任务ID
     */
    @Column(nullable = false)
    private Long taskId;

    /**
     * 帧号
     */
    @Column(nullable = false)
    private Integer frameNumber;

    /**
     * 时间戳（秒，相对于视频开始时间）
     */
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal timestamp;

    /**
     * 熔池亮度值（灰度值最高10%像素的平均值）
     */
    @Column(precision = 10, scale = 3)
    private BigDecimal brightness;

    /**
     * 熔池面积（像素）
     */
    @Column
    private Integer poolArea;

    /**
     * 熔池周长（像素）
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal poolPerimeter;
}
