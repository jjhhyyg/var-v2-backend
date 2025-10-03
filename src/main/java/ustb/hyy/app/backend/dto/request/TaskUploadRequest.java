package ustb.hyy.app.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 任务上传请求
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
public class TaskUploadRequest {

    /**
     * 视频文件（支持mp4/avi/mov，最大2GB）
     */
    @NotNull(message = "视频文件不能为空")
    private MultipartFile video;

    /**
     * 任务名称（默认使用文件名）
     */
    private String name;

    /**
     * 超时比例（如"1:4"，默认"1:4"）
     */
    @Pattern(regexp = "^\\d+:\\d+$", message = "超时比例格式不正确，应为\"分子:分母\"格式，如\"1:4\"")
    private String timeoutRatio;

    /**
     * 置信度阈值（0.1~0.9，默认0.5）
     */
    @DecimalMin(value = "0.1", message = "置信度阈值不能小于0.1")
    @DecimalMax(value = "0.9", message = "置信度阈值不能大于0.9")
    private Double confidenceThreshold;

    /**
     * IoU阈值（0.3~0.7，默认0.45）
     */
    @DecimalMin(value = "0.3", message = "IoU阈值不能小于0.3")
    @DecimalMax(value = "0.7", message = "IoU阈值不能大于0.7")
    private Double iouThreshold;
}
