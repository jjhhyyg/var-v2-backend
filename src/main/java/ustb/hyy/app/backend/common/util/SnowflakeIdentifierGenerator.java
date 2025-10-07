package ustb.hyy.app.backend.common.util;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.stereotype.Component;

/**
 * Hibernate自定义ID生成器（使用雪花算法）
 * <p>
 * 配合 @SnowflakeId 注解使用，通过 @IdGeneratorType 注册
 * 替代了旧的 @GenericGenerator 方式
 * </p>
 *
 * @author 侯阳洋
 * @since 2025-10-01
 * @see ustb.hyy.app.backend.common.annotation.SnowflakeId
 */
@Component
public class SnowflakeIdentifierGenerator implements IdentifierGenerator {

    private static SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 设置雪花算法ID生成器实例（由Spring容器注入）
     */
    public static void setSnowflakeIdGenerator(SnowflakeIdGenerator generator) {
        snowflakeIdGenerator = generator;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        if (snowflakeIdGenerator == null) {
            throw new IllegalStateException("SnowflakeIdGenerator未初始化，请检查配置");
        }
        return snowflakeIdGenerator.nextId();
    }
}
