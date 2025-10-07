-- 添加暂停/继续功能相关字段
-- 作者: AI Assistant
-- 日期: 2025-10-06

-- 1. 在TaskStatus枚举中添加PAUSED状态（已在代码中完成，数据库会自动支持）

-- 2. 为analysis_tasks表添加暂停相关字段
ALTER TABLE analysis_tasks
ADD COLUMN paused_at TIMESTAMP,
ADD COLUMN resumed_at TIMESTAMP,
ADD COLUMN last_processed_frame INTEGER DEFAULT 0;

-- 3. 添加注释
COMMENT ON COLUMN analysis_tasks.paused_at IS '任务暂停时间';
COMMENT ON COLUMN analysis_tasks.resumed_at IS '任务恢复时间';
COMMENT ON COLUMN analysis_tasks.last_processed_frame IS '最后处理的帧数（用于断点续传）';
