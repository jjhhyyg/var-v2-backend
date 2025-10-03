package ustb.hyy.app.backend.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import ustb.hyy.app.backend.dto.response.TaskStatusResponse;

import java.util.concurrent.TimeUnit;

/**
 * 任务进度缓存服务
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskProgressCache {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PROGRESS_KEY_PREFIX = "task:progress:";
    private static final String STATUS_KEY_PREFIX = "task:status:";
    private static final long CACHE_EXPIRE_HOURS = 1L;

    /**
     * 缓存任务进度
     *
     * @param taskId   任务ID
     * @param progress 进度信息
     */
    public void cacheProgress(Long taskId, TaskStatusResponse progress) {
        try {
            String key = PROGRESS_KEY_PREFIX + taskId;
            redisTemplate.opsForValue().set(key, progress, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("任务进度已缓存，taskId: {}", taskId);
        } catch (Exception e) {
            log.error("缓存任务进度失败，taskId: {}", taskId, e);
        }
    }

    /**
     * 获取任务进度
     *
     * @param taskId 任务ID
     * @return 进度信息，不存在则返回null
     */
    public TaskStatusResponse getProgress(Long taskId) {
        try {
            String key = PROGRESS_KEY_PREFIX + taskId;
            return (TaskStatusResponse) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取任务进度缓存失败，taskId: {}", taskId, e);
            return null;
        }
    }

    /**
     * 缓存任务状态
     *
     * @param taskId 任务ID
     * @param status 任务状态
     */
    public void cacheStatus(Long taskId, String status) {
        try {
            String key = STATUS_KEY_PREFIX + taskId;
            redisTemplate.opsForValue().set(key, status, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("任务状态已缓存，taskId: {}, status: {}", taskId, status);
        } catch (Exception e) {
            log.error("缓存任务状态失败，taskId: {}", taskId, e);
        }
    }

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态，不存在则返回null
     */
    public String getStatus(Long taskId) {
        try {
            String key = STATUS_KEY_PREFIX + taskId;
            return (String) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取任务状态缓存失败，taskId: {}", taskId, e);
            return null;
        }
    }

    /**
     * 删除任务进度缓存
     *
     * @param taskId 任务ID
     */
    public void deleteProgress(Long taskId) {
        try {
            String progressKey = PROGRESS_KEY_PREFIX + taskId;
            String statusKey = STATUS_KEY_PREFIX + taskId;
            redisTemplate.delete(progressKey);
            redisTemplate.delete(statusKey);
            log.debug("任务进度缓存已删除，taskId: {}", taskId);
        } catch (Exception e) {
            log.error("删除任务进度缓存失败，taskId: {}", taskId, e);
        }
    }

    /**
     * 设置超时预警标志
     *
     * @param taskId 任务ID
     */
    public void setTimeoutWarning(Long taskId) {
        try {
            String key = "task:timeout:warning:" + taskId;
            redisTemplate.opsForValue().set(key, true, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("任务超时预警已设置，taskId: {}", taskId);
        } catch (Exception e) {
            log.error("设置任务超时预警失败，taskId: {}", taskId, e);
        }
    }

    /**
     * 检查是否有超时预警
     *
     * @param taskId 任务ID
     * @return true=有超时预警，false=无超时预警
     */
    public boolean hasTimeoutWarning(Long taskId) {
        try {
            String key = "task:timeout:warning:" + taskId;
            Boolean warning = (Boolean) redisTemplate.opsForValue().get(key);
            return Boolean.TRUE.equals(warning);
        } catch (Exception e) {
            log.error("检查任务超时预警失败，taskId: {}", taskId, e);
            return false;
        }
    }
}
