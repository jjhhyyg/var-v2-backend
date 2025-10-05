-- VAR熔池视频分析系统 - 添加全局分析字段和重命名亮度字段
-- 作者：侯阳洋
-- 日期：2025-10-05

-- 1. 在 analysis_tasks 表添加 global_analysis 字段
-- 用于存储全局频率分析结果（闪烁频率、面积频率、周长频率、圆度等）
ALTER TABLE analysis_tasks
ADD COLUMN global_analysis JSONB;

-- 添加字段注释
COMMENT ON COLUMN analysis_tasks.global_analysis IS '全局频率分析结果（JSON格式）：包含闪烁、面积、周长的频率分析和圆度';

-- 2. 重命名 dynamic_metrics 表的 flicker_frequency 字段为 brightness
-- 因为现在每帧存储的是亮度值，而不是频率（频率通过FFT全局计算）
ALTER TABLE dynamic_metrics
RENAME COLUMN flicker_frequency TO brightness;

-- 更新字段注释
COMMENT ON COLUMN dynamic_metrics.brightness IS '熔池亮度值（灰度值最高10%像素的平均值）';
