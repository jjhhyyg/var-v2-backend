package ustb.hyy.app.backend.domain.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Getter
public enum TaskStatus {
    /**
     * 待处理
     */
    PENDING("待处理"),

    /**
     * 预处理中（视频解析、帧提取、元数据读取）
     */
    PREPROCESSING("预处理中"),

    /**
     * 分析中（AI模型推理、目标检测与追踪）
     */
    ANALYZING("分析中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 已完成（超时）
     */
    COMPLETED_TIMEOUT("已完成（超时）"),

    /**
     * 失败
     */
    FAILED("失败");

    private final String description;

    TaskStatus(String description) {
        this.description = description;
    }
}
