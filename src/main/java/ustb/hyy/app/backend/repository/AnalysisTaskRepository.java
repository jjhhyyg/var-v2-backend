package ustb.hyy.app.backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ustb.hyy.app.backend.domain.entity.AnalysisTask;
import ustb.hyy.app.backend.domain.enums.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分析任务Repository
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Repository
public interface AnalysisTaskRepository extends JpaRepository<AnalysisTask, Long> {

    /**
     * 根据状态分页查询任务
     *
     * @param status   任务状态
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<AnalysisTask> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * 根据状态查询任务列表
     *
     * @param status 任务状态
     * @return 任务列表
     */
    List<AnalysisTask> findByStatus(TaskStatus status);

    /**
     * 查询超时的处理中任务
     *
     * @param currentTime 当前时间
     * @return 超时任务列表
     */
    @Query(value = "SELECT * FROM analysis_tasks t WHERE t.status IN ('PREPROCESSING', 'ANALYZING') " +
            "AND t.is_timeout = false " +
            "AND t.started_at IS NOT NULL " +
            "AND EXTRACT(EPOCH FROM (:currentTime - t.started_at)) > t.timeout_threshold",
            nativeQuery = true)
    List<AnalysisTask> findTimeoutTasks(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计各状态任务数量
     *
     * @return 状态统计
     */
    @Query("SELECT t.status, COUNT(t) FROM AnalysisTask t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * 根据创建时间范围查询任务
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param pageable  分页参数
     * @return 任务分页列表
     */
    Page<AnalysisTask> findByCreatedAtBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
}
