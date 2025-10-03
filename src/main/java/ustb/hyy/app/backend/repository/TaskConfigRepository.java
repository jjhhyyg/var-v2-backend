package ustb.hyy.app.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ustb.hyy.app.backend.domain.entity.TaskConfig;

import java.util.Optional;

/**
 * 任务配置Repository
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Repository
public interface TaskConfigRepository extends JpaRepository<TaskConfig, Long> {

    /**
     * 根据任务ID查询配置
     *
     * @param taskId 任务ID
     * @return 任务配置
     */
    Optional<TaskConfig> findByTaskId(Long taskId);

    /**
     * 根据任务ID删除配置
     *
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);
}
