package ustb.hyy.app.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 进度更新请求（AI模块回调）
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
public class ProgressUpdateRequest {

    /**
     * 任务状态
     */
    @NotBlank(message = "任务状态不能为空")
    @Pattern(regexp = "^(PREPROCESSING|ANALYZING|COMPLETED|COMPLETED_TIMEOUT|FAILED)$",
            message = "任务状态无效")
    private String status;

    /**
     * 进度（0.0~1.0）
     */
    @DecimalMin(value = "0.0", message = "进度不能小于0")
    @DecimalMax(value = "1.0", message = "进度不能大于1")
    private Double progress;

    /**
     * 当前帧号
     */
    @Min(value = 0, message = "当前帧号不能小于0")
    private Integer currentFrame;

    /**
     * 总帧数
     */
    @Min(value = 1, message = "总帧数不能小于1")
    private Integer totalFrames;

    /**
     * 当前阶段（preprocessing/analyzing）
     */
    private String phase;

    /**
     * 预处理耗时（秒）
     */
    @Min(value = 0, message = "预处理耗时不能小于0")
    private Integer preprocessingDuration;

    /**
     * 分析已耗时（秒）
     */
    @Min(value = 0, message = "分析已耗时不能小于0")
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
     * 失败原因（仅status=FAILED时）
     */
    private String failureReason;
}
