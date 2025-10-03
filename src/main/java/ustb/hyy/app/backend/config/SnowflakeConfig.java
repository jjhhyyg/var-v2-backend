package ustb.hyy.app.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ustb.hyy.app.backend.common.util.SnowflakeIdGenerator;
import ustb.hyy.app.backend.common.util.SnowflakeIdentifierGenerator;

/**
 * 雪花算法ID生成器配置
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Configuration
@ConfigurationProperties(prefix = "app.snowflake")
@Data
public class SnowflakeConfig {

    /**
     * 数据中心ID（0-31）
     */
    private Long datacenterId = 0L;

    /**
     * 工作机器ID（0-31）
     */
    private Long workerId = 0L;

    /**
     * 创建雪花算法ID生成器Bean
     */
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        SnowflakeIdGenerator generator = new SnowflakeIdGenerator(datacenterId, workerId);
        // 注入到Hibernate ID生成器
        SnowflakeIdentifierGenerator.setSnowflakeIdGenerator(generator);
        return generator;
    }
}
