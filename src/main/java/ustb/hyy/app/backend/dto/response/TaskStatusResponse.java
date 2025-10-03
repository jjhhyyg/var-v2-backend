package ustb.hyy.app.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务状态响应DTO
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskStatusResponse {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 当前阶段（preprocessing/analyzing）
     */
    private String phase;

    /**
     * 进度（0.0~1.0）
     */
    private Double progress;

    /**
     * 当前帧号
     */
    private Integer currentFrame;

    /**
     * 总帧数
     */
    private Integer totalFrames;

    /**
     * 预处理耗时（秒）
     */
    private Integer preprocessingDuration;

    /**
     * 分析已耗时（秒）
     */
    private Integer analyzingElapsedTime;

    /**
     * 是否超时
     */
    private Boolean isTimeout;

    /**
     * 超时预警
     */
    private Boolean timeoutWarning;

    /**
     * 失败原因
     */
    private String failureReason;
}
