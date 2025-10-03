package ustb.hyy.app.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 任务配置实体
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Entity
@Table(name = "task_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskConfig extends BaseEntity {

    /**
     * 关联的任务
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private AnalysisTask task;

    /**
     * 超时比例（格式：分子:分母，如"1:4"）
     */
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String timeoutRatio = "1:4";

    /**
     * 置信度阈值（0.1-0.9）
     */
    @Column(nullable = false)
    @Builder.Default
    private Double confidenceThreshold = 0.5;

    /**
     * IoU阈值（0.3-0.7）
     */
    @Column(nullable = false)
    @Builder.Default
    private Double iouThreshold = 0.45;

    /**
     * 模型版本（如"yolov11n"）
     */
    @Column(length = 50)
    @Builder.Default
    private String modelVersion = "yolov11n";
}
