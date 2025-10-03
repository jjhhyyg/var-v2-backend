package ustb.hyy.app.backend.domain.enums;

import lombok.Getter;

/**
 * 异常事件类型枚举
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Getter
public enum EventType {
    /**
     * 熔池未到边（状态检测）
     */
    POOL_NOT_REACHED("熔池未到边"),

    /**
     * 电极形成粘连物
     */
    ADHESION_FORMED("电极形成粘连物"),

    /**
     * 电极粘连物脱落
     */
    ADHESION_DROPPED("电极粘连物脱落"),

    /**
     * 锭冠脱落
     */
    CROWN_DROPPED("锭冠脱落"),

    /**
     * 辉光（电弧异常）
     */
    GLOW("辉光"),

    /**
     * 边弧/侧弧（电弧异常）
     */
    SIDE_ARC("边弧"),

    /**
     * 爬弧（电弧异常）
     */
    CREEPING_ARC("爬弧");

    private final String description;

    EventType(String description) {
        this.description = description;
    }
}
