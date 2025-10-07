package ustb.hyy.app.backend.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import ustb.hyy.app.backend.common.util.SnowflakeIdentifierGenerator;

/**
 * 雪花算法ID生成器注解
 * <p>
 * 使用现代化的 @IdGeneratorType 方式定义自定义ID生成器
 * 替代旧的 @GenericGenerator 注解
 * </p>
 * 
 * 使用示例:
 * <pre>
 * {@code
 * @Id
 * @GeneratedValue
 * @SnowflakeId
 * private Long id;
 * }
 * </pre>
 *
 * @author 侯阳洋
 * @since 2025-10-06
 */
@IdGeneratorType(SnowflakeIdentifierGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface SnowflakeId {
}
