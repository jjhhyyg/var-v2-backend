-- 添加累计暂停时长字段
-- 作者: AI Assistant
-- 日期: 2025-10-06
-- 目的: 支持暂停时间排除在总处理时间之外的功能

-- 为analysis_tasks表添加累计暂停时长字段
ALTER TABLE analysis_tasks
ADD COLUMN total_paused_duration INTEGER DEFAULT 0;

-- 添加注释
COMMENT ON COLUMN analysis_tasks.total_paused_duration IS '累计暂停时长（秒），用于计算净处理时间，排除暂停期间的时间';
