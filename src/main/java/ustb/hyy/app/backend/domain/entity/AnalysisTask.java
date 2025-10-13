package ustb.hyy.app.backend.domain.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ustb.hyy.app.backend.domain.enums.TaskStatus;

/**
 * 分析任务实体
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Entity
@Table(name = "analysis_tasks", indexes = {
        @Index(name = "idx_status_created", columnList = "status,created_at"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisTask extends BaseEntity {

    /**
     * 任务名称
     */
    @Column(nullable = false, length = 255)
    private String name;

    /**
     * 原始文件名（用户上传时的真实文件名）
     */
    @Column(length = 255)
    private String originalFilename;

    /**
     * 视频文件路径
     */
    @Column(nullable = false, length = 500)
    private String videoPath;

    /**
     * 结果视频文件路径（带标注的视频）
     */
    @Column(length = 500)
    private String resultVideoPath;

    /**
     * 预处理后的视频文件路径
     */
    @Column(length = 500)
    private String preprocessedVideoPath;

    /**
     * 视频时长（秒）
     */
    @Column(nullable = false)
    private Integer videoDuration;

    /**
     * 任务状态
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    /**
     * 超时阈值（秒，根据超时比例计算）
     */
    @Column(nullable = false)
    private Integer timeoutThreshold;

    /**
     * 是否超时
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isTimeout = false;

    /**
     * 任务开始时间（进入PREPROCESSING状态时）
     */
    @Column
    private LocalDateTime startedAt;

    /**
     * 预处理完成时间（PREPROCESSING -> ANALYZING）
     */
    @Column
    private LocalDateTime preprocessingCompletedAt;

    /**
     * 任务完成时间
     */
    @Column
    private LocalDateTime completedAt;

    /**
     * 失败原因
     */
    @Column(length = 1000)
    private String failureReason;

    /**
     * 全局频率分析结果（JSON格式）
     * 包含闪烁、面积、周长的频率分析和圆度
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> globalAnalysis;

    /**
     * 一对一关联任务配置
     */
    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TaskConfig config;
}
