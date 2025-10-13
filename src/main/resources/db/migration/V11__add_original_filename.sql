-- 添加 original_filename 字段到 analysis_tasks 表
-- 执行日期: 2025-10-13
-- 说明: 用于存储用户上传时的真实文件名，文件实际存储使用 UUID_timestamp 格式

-- 添加新字段
ALTER TABLE analysis_tasks ADD COLUMN IF NOT EXISTS original_filename VARCHAR(255);

-- 添加字段注释（PostgreSQL）
COMMENT ON COLUMN analysis_tasks.original_filename IS '原始文件名（用户上传时的真实文件名）';

-- 可选：从现有的 name 字段复制值到 original_filename
-- 仅在需要为旧数据填充该字段时执行
-- UPDATE analysis_tasks
-- SET original_filename = name
-- WHERE original_filename IS NULL AND name IS NOT NULL;
