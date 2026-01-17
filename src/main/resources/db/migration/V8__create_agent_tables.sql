-- =====================================================
-- V8: 创建Agent模块相关表
-- 创建时间: 2026-01-17
-- 说明: 支持Agent配置管理和执行日志记录
-- =====================================================

-- =====================================================
-- 1. agent_config - Agent配置表
-- =====================================================
CREATE TABLE IF NOT EXISTS agent_config (
    id BIGINT PRIMARY KEY,
    agent_code VARCHAR(50) NOT NULL,
    agent_name VARCHAR(100) NOT NULL,
    description TEXT,
    version VARCHAR(20) NOT NULL,
    agent_scope_config TEXT,
    config TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    is_deleted INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 创建唯一索引
CREATE UNIQUE INDEX uk_agent_code ON agent_config(agent_code) WHERE is_deleted = 0;

-- 创建普通索引
CREATE INDEX idx_agent_config_status ON agent_config(status);
CREATE INDEX idx_agent_config_is_deleted ON agent_config(is_deleted);

-- 添加注释
COMMENT ON TABLE agent_config IS 'Agent配置表';

-- =====================================================
-- 2. agent_execution_log - Agent执行日志表
-- =====================================================
CREATE TABLE IF NOT EXISTS agent_execution_log (
    id BIGINT PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    input TEXT,
    output TEXT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    execution_time_ms BIGINT,
    is_deleted INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 创建索引
CREATE INDEX idx_agent_execution_log_agent_id ON agent_execution_log(agent_id);
CREATE INDEX idx_agent_execution_log_session_id ON agent_execution_log(session_id);
CREATE INDEX idx_agent_execution_log_status ON agent_execution_log(status);
CREATE INDEX idx_agent_execution_log_create_time ON agent_execution_log(create_time);
CREATE INDEX idx_agent_execution_log_is_deleted ON agent_execution_log(is_deleted);

-- 添加注释
COMMENT ON TABLE agent_execution_log IS 'Agent执行日志表';
