-- 为 task_configs 表添加 frame_rate 字段
-- 用于存储视频帧率（由FFmpeg解析得到）

ALTER TABLE task_configs ADD COLUMN frame_rate DOUBLE PRECISION NOT NULL DEFAULT 25.0;

COMMENT ON COLUMN task_configs.frame_rate IS '视频帧率（由FFmpeg解析得到，用于时间戳计算和帧号转换）';
