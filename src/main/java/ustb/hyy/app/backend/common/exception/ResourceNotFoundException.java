package ustb.hyy.app.backend.common.exception;

/**
 * 资源未找到异常
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(404, message);
    }

    public ResourceNotFoundException(String resourceName, Object resourceId) {
        super(404, String.format("%s未找到: %s", resourceName, resourceId));
    }
}
