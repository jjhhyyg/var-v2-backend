-- VAR熔池视频分析系统 - 数据库初始化脚本
-- 作者：侯阳洋
-- 日期：2025-10-01

-- 创建分析任务表
CREATE TABLE analysis_tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    video_path VARCHAR(500) NOT NULL,
    video_duration INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    timeout_threshold INTEGER NOT NULL,
    is_timeout BOOLEAN NOT NULL DEFAULT FALSE,
    started_at TIMESTAMP,
    preprocessing_completed_at TIMESTAMP,
    completed_at TIMESTAMP,
    failure_reason VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建任务配置表
CREATE TABLE task_configs (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL UNIQUE,
    timeout_ratio VARCHAR(10) NOT NULL DEFAULT '1:4',
    confidence_threshold DOUBLE PRECISION NOT NULL DEFAULT 0.5,
    iou_threshold DOUBLE PRECISION NOT NULL DEFAULT 0.45,
    model_version VARCHAR(50) DEFAULT 'yolov11n',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_config_task FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE
);

-- 创建动态参数表
CREATE TABLE dynamic_metrics (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    frame_number INTEGER NOT NULL,
    timestamp NUMERIC(10, 3) NOT NULL,
    flicker_frequency NUMERIC(10, 3),
    pool_area INTEGER,
    pool_perimeter NUMERIC(10, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dynamic_metric_task FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE
);

-- 创建异常事件表
CREATE TABLE anomaly_events (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    start_frame INTEGER NOT NULL,
    end_frame INTEGER NOT NULL,
    object_id INTEGER,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_anomaly_event_task FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE
);

-- 创建追踪物体表
CREATE TABLE tracking_objects (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    object_id INTEGER NOT NULL,
    category VARCHAR(30) NOT NULL,
    first_frame INTEGER NOT NULL,
    last_frame INTEGER NOT NULL,
    trajectory JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tracking_object_task FOREIGN KEY (task_id) REFERENCES analysis_tasks(id) ON DELETE CASCADE
);

-- 创建索引
-- analysis_tasks索引
CREATE INDEX idx_status_created ON analysis_tasks(status, created_at);
CREATE INDEX idx_created_at ON analysis_tasks(created_at);

-- dynamic_metrics索引
CREATE INDEX idx_task_frame ON dynamic_metrics(task_id, frame_number);
CREATE INDEX idx_task_timestamp ON dynamic_metrics(task_id, timestamp);

-- anomaly_events索引
CREATE INDEX idx_task_type ON anomaly_events(task_id, event_type);
CREATE INDEX idx_task_start_frame ON anomaly_events(task_id, start_frame);

-- tracking_objects索引
CREATE INDEX idx_task_object ON tracking_objects(task_id, object_id);
CREATE INDEX idx_task_category ON tracking_objects(task_id, category);

-- 添加表注释
COMMENT ON TABLE analysis_tasks IS '分析任务表';
COMMENT ON TABLE task_configs IS '任务配置表';
COMMENT ON TABLE dynamic_metrics IS '动态参数表（时序数据）';
COMMENT ON TABLE anomaly_events IS '异常事件表';
COMMENT ON TABLE tracking_objects IS '追踪物体表（BoTSORT追踪结果）';

-- 添加列注释
COMMENT ON COLUMN analysis_tasks.status IS '任务状态：PENDING, PREPROCESSING, ANALYZING, COMPLETED, COMPLETED_TIMEOUT, FAILED';
COMMENT ON COLUMN analysis_tasks.is_timeout IS '是否超时';
COMMENT ON COLUMN task_configs.timeout_ratio IS '超时比例（格式：分子:分母，如1:4）';
COMMENT ON COLUMN task_configs.confidence_threshold IS '置信度阈值（0.1-0.9）';
COMMENT ON COLUMN task_configs.iou_threshold IS 'IoU阈值（0.3-0.7）';
COMMENT ON COLUMN dynamic_metrics.timestamp IS '时间戳（秒，相对于视频开始时间）';
COMMENT ON COLUMN dynamic_metrics.flicker_frequency IS '熔池闪烁频率（Hz）';
COMMENT ON COLUMN dynamic_metrics.pool_area IS '熔池面积（像素）';
COMMENT ON COLUMN dynamic_metrics.pool_perimeter IS '熔池周长（像素）';
COMMENT ON COLUMN anomaly_events.event_type IS '事件类型：POOL_NOT_REACHED, ADHESION_FORMED, ADHESION_DROPPED, CROWN_DROPPED, GLOW, SIDE_ARC, CREEPING_ARC';
COMMENT ON COLUMN anomaly_events.metadata IS '元数据（JSON格式，存储位置、轨迹、置信度等）';
COMMENT ON COLUMN tracking_objects.category IS '物体类别：POOL_NOT_REACHED, ADHESION, CROWN, GLOW, SIDE_ARC, CREEPING_ARC';
COMMENT ON COLUMN tracking_objects.trajectory IS '轨迹数据（JSON数组）';
