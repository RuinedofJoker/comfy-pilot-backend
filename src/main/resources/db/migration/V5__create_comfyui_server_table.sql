-- ComfyUI服务模块数据库迁移脚本
-- 创建时间: 2026-01-16

-- 1. 创建ComfyUI服务表
CREATE TABLE IF NOT EXISTS comfyui_server (
    id BIGINT PRIMARY KEY,
    server_key VARCHAR(100) NOT NULL,
    server_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    base_url VARCHAR(255) NOT NULL,
    auth_mode VARCHAR(20),
    api_key VARCHAR(255),
    timeout_seconds INT NOT NULL DEFAULT 30,
    max_retries INT NOT NULL DEFAULT 3,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_health_check_time TIMESTAMP,
    health_status VARCHAR(20),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL,
    is_deleted BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_server_key UNIQUE (server_key)
);

COMMENT ON TABLE comfyui_server IS 'ComfyUI服务表';
COMMENT ON COLUMN comfyui_server.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN comfyui_server.server_key IS '服务唯一标识符';
COMMENT ON COLUMN comfyui_server.server_name IS '服务名称';
COMMENT ON COLUMN comfyui_server.description IS '服务描述';
COMMENT ON COLUMN comfyui_server.base_url IS 'ComfyUI服务地址';
COMMENT ON COLUMN comfyui_server.auth_mode IS '认证模式（null-无认证，basic_auth-Basic Auth认证）';
COMMENT ON COLUMN comfyui_server.api_key IS 'API密钥';
COMMENT ON COLUMN comfyui_server.timeout_seconds IS '请求超时时间（秒）';
COMMENT ON COLUMN comfyui_server.max_retries IS '最大重试次数';
COMMENT ON COLUMN comfyui_server.is_enabled IS '是否启用';
COMMENT ON COLUMN comfyui_server.last_health_check_time IS '最后健康检查时间';
COMMENT ON COLUMN comfyui_server.health_status IS '健康状态（HEALTHY-健康，UNHEALTHY-不健康，UNKNOWN-未知）';
COMMENT ON COLUMN comfyui_server.create_time IS '创建时间';
COMMENT ON COLUMN comfyui_server.create_by IS '创建人ID';
COMMENT ON COLUMN comfyui_server.update_time IS '更新时间';
COMMENT ON COLUMN comfyui_server.update_by IS '更新人ID';
COMMENT ON COLUMN comfyui_server.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- 2. 创建索引
CREATE INDEX IF NOT EXISTS idx_comfyui_server_is_enabled ON comfyui_server(is_enabled);
CREATE INDEX IF NOT EXISTS idx_comfyui_server_health_status ON comfyui_server(health_status);
