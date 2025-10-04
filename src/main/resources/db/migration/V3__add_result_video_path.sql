-- 添加结果视频路径字段
-- 作者：侯阳洋
-- 日期：2025-10-04
-- 说明：为 analysis_tasks 表添加 result_video_path 字段，用于存储带标注的结果视频路径

-- 添加结果视频路径字段
ALTER TABLE analysis_tasks 
ADD COLUMN result_video_path VARCHAR(500);

-- 添加字段注释
COMMENT ON COLUMN analysis_tasks.result_video_path IS '结果视频文件路径（带标注的视频）';
