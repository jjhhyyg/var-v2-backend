package ustb.hyy.app.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ustb.hyy.app.backend.domain.entity.AnomalyEvent;
import ustb.hyy.app.backend.domain.enums.EventType;

import java.util.List;

/**
 * 异常事件Repository
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Repository
public interface AnomalyEventRepository extends JpaRepository<AnomalyEvent, Long> {

    /**
     * 根据任务ID查询所有异常事件（按起始帧号排序）
     *
     * @param taskId 任务ID
     * @return 异常事件列表
     */
    List<AnomalyEvent> findByTaskIdOrderByStartFrameAsc(Long taskId);

    /**
     * 根据任务ID和事件类型查询
     *
     * @param taskId    任务ID
     * @param eventType 事件类型
     * @return 异常事件列表
     */
    List<AnomalyEvent> findByTaskIdAndEventTypeOrderByStartFrameAsc(Long taskId, EventType eventType);

    /**
     * 根据任务ID删除所有异常事件
     *
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

    /**
     * 统计任务的各类型事件数量
     *
     * @param taskId 任务ID
     * @return [eventType, count]
     */
    @Query("SELECT e.eventType, COUNT(e) FROM AnomalyEvent e WHERE e.taskId = :taskId GROUP BY e.eventType")
    List<Object[]> countByEventType(Long taskId);

    /**
     * 批量保存异常事件
     *
     * @param events 异常事件列表
     * @return 保存后的列表
     */
    @Override
    <S extends AnomalyEvent> List<S> saveAll(Iterable<S> events);
}
