package ustb.hyy.app.backend.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体基类，包含通用字段
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    /**
     * 主键ID（使用雪花算法生成）
     */
    @Id
    @GeneratedValue(generator = "snowflake-id-generator")
    @GenericGenerator(
            name = "snowflake-id-generator",
            type = ustb.hyy.app.backend.common.util.SnowflakeIdentifierGenerator.class
    )
    private Long id;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
