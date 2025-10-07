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
    private String modelVersion = "yolov11m";

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
}
