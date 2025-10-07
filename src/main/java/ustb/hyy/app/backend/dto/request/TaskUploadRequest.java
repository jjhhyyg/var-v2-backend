package ustb.hyy.app.backend.dto.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

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
     * 是否启用视频预处理（默认false）
     */
    private Boolean enablePreprocessing;

    /**
     * 预处理强度（mild/moderate/strong，默认moderate）
     */
    @Pattern(regexp = "^(mild|moderate|strong)$", message = "预处理强度只能为：mild, moderate, strong")
    private String preprocessingStrength;

    /**
     * 是否启用熔池增强（默认true）
     */
    private Boolean preprocessingEnhancePool;
}
