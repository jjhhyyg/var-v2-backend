-- VAR熔池视频分析系统 - 修改ID为雪花算法
-- 作者：侯阳洋
-- 日期：2025-10-01
-- 说明：将所有表的自增ID改为使用雪花算法生成的BIGINT ID

-- 1. 删除所有外键约束
ALTER TABLE task_configs DROP CONSTRAINT IF EXISTS fk_task_config_task;
ALTER TABLE dynamic_metrics DROP CONSTRAINT IF EXISTS fk_dynamic_metric_task;
ALTER TABLE anomaly_events DROP CONSTRAINT IF EXISTS fk_anomaly_event_task;
ALTER TABLE tracking_objects DROP CONSTRAINT IF EXISTS fk_tracking_object_task;

-- 2. 修改所有表的ID列类型
-- analysis_tasks表
ALTER TABLE analysis_tasks ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS analysis_tasks_id_seq;
ALTER TABLE analysis_tasks ALTER COLUMN id TYPE BIGINT;

-- task_configs表
ALTER TABLE task_configs ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS task_configs_id_seq;
ALTER TABLE task_configs ALTER COLUMN id TYPE BIGINT;

-- dynamic_metrics表
ALTER TABLE dynamic_metrics ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS dynamic_metrics_id_seq;
ALTER TABLE dynamic_metrics ALTER COLUMN id TYPE BIGINT;

-- anomaly_events表
ALTER TABLE anomaly_events ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS anomaly_events_id_seq;
ALTER TABLE anomaly_events ALTER COLUMN id TYPE BIGINT;

-- tracking_objects表
ALTER TABLE tracking_objects ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS tracking_objects_id_seq;
ALTER TABLE tracking_objects ALTER COLUMN id TYPE BIGINT;

-- 3. 重新创建外键约束
ALTER TABLE task_configs
    ADD CONSTRAINT fk_task_config_task
    FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE;

ALTER TABLE dynamic_metrics
    ADD CONSTRAINT fk_dynamic_metric_task
    FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE;

ALTER TABLE anomaly_events
    ADD CONSTRAINT fk_anomaly_event_task
    FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE;

ALTER TABLE tracking_objects
    ADD CONSTRAINT fk_tracking_object_task
    FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE;

-- 4. 添加迁移说明注释
COMMENT ON TABLE analysis_tasks IS '分析任务表（ID使用雪花算法生成）';
COMMENT ON TABLE task_configs IS '任务配置表（ID使用雪花算法生成）';
COMMENT ON TABLE dynamic_metrics IS '动态参数表（ID使用雪花算法生成）';
COMMENT ON TABLE anomaly_events IS '异常事件表（ID使用雪花算法生成）';
COMMENT ON TABLE tracking_objects IS '追踪物体表（ID使用雪花算法生成）';
