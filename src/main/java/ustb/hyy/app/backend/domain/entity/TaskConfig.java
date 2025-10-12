package ustb.hyy.app.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
     * 模型版本（如"yolov11n"）
     */
    @Column(length = 50)
    @Builder.Default
    private String modelVersion = null;

    /**
     * 是否启用视频预处理
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enablePreprocessing = false;

    /**
     * 预处理强度（mild, moderate, strong）
     */
    @Column(length = 10)
    @Builder.Default
    private String preprocessingStrength = "moderate";

    /**
     * 是否启用熔池特定增强
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean preprocessingEnhancePool = true;

    /**
     * 是否启用追踪轨迹合并（解决粘连物/锭冠脱落时的ID断裂问题）
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enableTrackingMerge = false;

    /**
     * 追踪合并策略（auto, adhesion, ingot_crown, conservative, aggressive）
     */
    @Column(length = 20)
    @Builder.Default
    private String trackingMergeStrategy = "auto";

    /**
     * 视频帧率（由FFmpeg解析得到，用于时间戳计算和帧号转换）
     */
    @Column(nullable = false)
    @Builder.Default
    private Double frameRate = 25.0;
}
