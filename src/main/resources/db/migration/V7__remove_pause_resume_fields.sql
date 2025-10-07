-- 移除暂停/恢复功能相关字段
-- 作者: AI Assistant
-- 日期: 2025-10-06
-- 目的: 移除任务暂停/恢复功能，简化系统

-- 从analysis_tasks表删除暂停/恢复相关字段
ALTER TABLE analysis_tasks
DROP COLUMN IF EXISTS paused_at,
DROP COLUMN IF EXISTS resumed_at,
DROP COLUMN IF EXISTS last_processed_frame,
DROP COLUMN IF EXISTS total_paused_duration;
