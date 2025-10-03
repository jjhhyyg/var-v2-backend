package ustb.hyy.app.backend.domain.enums;

import lombok.Getter;

/**
 * 追踪物体类别枚举（对应YOLOv11检测类别）
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Getter
public enum ObjectCategory {
    /**
     * 熔池未到边
     */
    POOL_NOT_REACHED(0, "熔池未到边"),

    /**
     * 粘连物
     */
    ADHESION(1, "粘连物"),

    /**
     * 锭冠
     */
    CROWN(2, "锭冠"),

    /**
     * 辉光
     */
    GLOW(3, "辉光"),

    /**
     * 边弧（侧弧）
     */
    SIDE_ARC(4, "边弧"),

    /**
     * 爬弧
     */
    CREEPING_ARC(5, "爬弧");

    /**
     * YOLO类别ID
     */
    private final int classId;

    /**
     * 类别描述
     */
    private final String description;

    ObjectCategory(int classId, String description) {
        this.classId = classId;
        this.description = description;
    }

    /**
     * 根据YOLO类别ID获取对应的ObjectCategory
     *
     * @param classId YOLO类别ID
     * @return ObjectCategory
     */
    public static ObjectCategory fromClassId(int classId) {
        for (ObjectCategory category : values()) {
            if (category.classId == classId) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown class ID: " + classId);
    }
}
