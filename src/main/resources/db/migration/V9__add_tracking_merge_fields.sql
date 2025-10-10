-- 添加追踪轨迹合并功能配置字段
-- 作者: AI Assistant
-- 日期: 2025-10-10
-- 目的: 支持可选的追踪轨迹合并算法，解决ID断裂问题

-- 为task_configs表添加追踪合并配置字段
ALTER TABLE task_configs
ADD COLUMN enable_tracking_merge BOOLEAN NOT NULL DEFAULT TRUE,
ADD COLUMN tracking_merge_strategy VARCHAR(20) DEFAULT 'auto';

-- 添加列注释
COMMENT ON COLUMN task_configs.enable_tracking_merge IS '是否启用追踪轨迹合并（解决粘连物/锭冠脱落时的ID断裂问题）';
COMMENT ON COLUMN task_configs.tracking_merge_strategy IS '合并策略：auto(自动), adhesion(粘连物), ingot_crown(锭冠), conservative(保守), aggressive(激进)';

-- 添加约束：检查合并策略的有效值
ALTER TABLE task_configs
ADD CONSTRAINT check_tracking_merge_strategy
CHECK (tracking_merge_strategy IN ('auto', 'adhesion', 'ingot_crown', 'conservative', 'aggressive'));
