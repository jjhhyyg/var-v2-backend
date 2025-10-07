-- 添加视频预处理功能相关字段
-- 作者: AI Assistant
-- 日期: 2025-10-07
-- 目的: 支持可选的视频预处理功能

-- 为task_configs表添加预处理配置字段
ALTER TABLE task_configs
ADD COLUMN enable_preprocessing BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN preprocessing_strength VARCHAR(10) DEFAULT 'moderate',
ADD COLUMN preprocessing_enhance_pool BOOLEAN NOT NULL DEFAULT TRUE;

-- 为analysis_tasks表添加预处理后视频路径字段
ALTER TABLE analysis_tasks
ADD COLUMN preprocessed_video_path VARCHAR(500);

-- 添加列注释
COMMENT ON COLUMN task_configs.enable_preprocessing IS '是否启用视频预处理';
COMMENT ON COLUMN task_configs.preprocessing_strength IS '预处理强度：mild(轻度), moderate(中度), strong(强度)';
COMMENT ON COLUMN task_configs.preprocessing_enhance_pool IS '是否启用熔池特定增强';
COMMENT ON COLUMN analysis_tasks.preprocessed_video_path IS '预处理后的视频文件路径（相对于codes目录）';

-- 添加约束：只有启用预处理时才能设置预处理强度
ALTER TABLE task_configs
ADD CONSTRAINT check_preprocessing_strength
CHECK (preprocessing_strength IN ('mild', 'moderate', 'strong'));
