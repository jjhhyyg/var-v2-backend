package ustb.hyy.app.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ustb.hyy.app.backend.domain.entity.DynamicMetric;

import java.util.List;

/**
 * 动态参数Repository
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Repository
public interface DynamicMetricRepository extends JpaRepository<DynamicMetric, Long> {

    /**
     * 根据任务ID查询所有动态参数（按帧号排序）
     *
     * @param taskId 任务ID
     * @return 动态参数列表
     */
    List<DynamicMetric> findByTaskIdOrderByFrameNumberAsc(Long taskId);

    /**
     * 根据任务ID和帧号范围查询
     *
     * @param taskId     任务ID
     * @param startFrame 起始帧号
     * @param endFrame   结束帧号
     * @return 动态参数列表
     */
    List<DynamicMetric> findByTaskIdAndFrameNumberBetweenOrderByFrameNumberAsc(
            Long taskId, Integer startFrame, Integer endFrame);

    /**
     * 根据任务ID删除所有动态参数
     *
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

    /**
     * 统计任务的动态参数数量
     *
     * @param taskId 任务ID
     * @return 数量
     */
    long countByTaskId(Long taskId);

    /**
     * 批量保存动态参数
     *
     * @param metrics 动态参数列表
     * @return 保存后的列表
     */
    @Override
    <S extends DynamicMetric> List<S> saveAll(Iterable<S> metrics);
}
