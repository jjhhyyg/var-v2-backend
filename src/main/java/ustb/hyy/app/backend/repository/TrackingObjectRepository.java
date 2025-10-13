package ustb.hyy.app.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ustb.hyy.app.backend.domain.entity.TrackingObject;
import ustb.hyy.app.backend.domain.enums.ObjectCategory;

/**
 * 追踪物体Repository
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Repository
public interface TrackingObjectRepository extends JpaRepository<TrackingObject, Long> {

    /**
     * 根据任务ID查询所有追踪物体（按首次出现帧号排序）
     *
     * @param taskId 任务ID
     * @return 追踪物体列表
     */
    List<TrackingObject> findByTaskIdOrderByFirstFrameAsc(Long taskId);

    /**
     * 根据任务ID和物体类别查询
     *
     * @param taskId   任务ID
     * @param category 物体类别
     * @return 追踪物体列表
     */
    List<TrackingObject> findByTaskIdAndCategoryOrderByFirstFrameAsc(Long taskId, ObjectCategory category);

    /**
     * 根据任务ID和物体ID查询
     *
     * @param taskId   任务ID
     * @param objectId BotSORT物体ID
     * @return 追踪物体
     */
    Optional<TrackingObject> findByTaskIdAndObjectId(Long taskId, Integer objectId);

    /**
     * 根据任务ID删除所有追踪物体
     *
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

    /**
     * 统计任务的各类别物体数量
     *
     * @param taskId 任务ID
     * @return [category, count]
     */
    @Query("SELECT t.category, COUNT(t) FROM TrackingObject t WHERE t.taskId = :taskId GROUP BY t.category")
    List<Object[]> countByCategory(Long taskId);

    /**
     * 批量保存追踪物体
     *
     * @param objects 追踪物体列表
     * @return 保存后的列表
     */
    @Override
    <S extends TrackingObject> List<S> saveAll(Iterable<S> objects);
}
