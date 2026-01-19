-- =====================================================
-- V7: 创建AI模型模块相关表
-- 创建时间: 2026-01-16
-- 更新时间: 2026-01-18
-- 说明: 支持远程API/本地接入 × LLM/Embedding等模型类型
--       API Key 存储在 model_provider.api_key 和 ai_model.model_config 中
-- =====================================================

-- =====================================================
-- 1. model_provider - 模型提供商表
-- =====================================================
CREATE TABLE IF NOT EXISTS model_provider (
    id BIGINT PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,
    api_base_url VARCHAR(500),
    api_key VARCHAR(500),
    description TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    is_deleted BIGINT NOT NULL DEFAULT 0
);

-- 创建索引
CREATE INDEX idx_provider_type ON model_provider(provider_type);
CREATE INDEX idx_provider_enabled ON model_provider(is_enabled);

-- 添加表注释
COMMENT ON TABLE model_provider IS '模型提供商表';

-- 添加列注释
COMMENT ON COLUMN model_provider.id IS '主键ID';
COMMENT ON COLUMN model_provider.provider_name IS '提供商名称';
COMMENT ON COLUMN model_provider.provider_type IS '提供商类型(openai/anthropic/aliyun/custom)';
COMMENT ON COLUMN model_provider.api_base_url IS 'API基础URL';
COMMENT ON COLUMN model_provider.api_key IS 'API密钥(可选，用于提供商级别的默认密钥)';
COMMENT ON COLUMN model_provider.description IS '描述信息';
COMMENT ON COLUMN model_provider.is_enabled IS '是否启用';
COMMENT ON COLUMN model_provider.create_time IS '创建时间';
COMMENT ON COLUMN model_provider.update_time IS '更新时间';
COMMENT ON COLUMN model_provider.create_by IS '创建人ID';
COMMENT ON COLUMN model_provider.update_by IS '更新人ID';
COMMENT ON COLUMN model_provider.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- =====================================================
-- 2. ai_model - AI模型表
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_model (
    id BIGINT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL,
    model_identifier VARCHAR(100) NOT NULL UNIQUE,
    access_type VARCHAR(50) NOT NULL,
    model_type VARCHAR(50) NOT NULL,
    model_source VARCHAR(50) NOT NULL DEFAULT 'remote_api',
    provider_id BIGINT,
    model_config TEXT,
    description TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    is_deleted BIGINT NOT NULL DEFAULT 0
);

-- 创建索引
CREATE UNIQUE INDEX uk_model_identifier ON ai_model(model_identifier);
CREATE INDEX idx_model_access_type ON ai_model(access_type);
CREATE INDEX idx_model_type ON ai_model(model_type);
CREATE INDEX idx_model_source ON ai_model(model_source);
CREATE INDEX idx_model_provider_id ON ai_model(provider_id);
CREATE INDEX idx_model_enabled ON ai_model(is_enabled);

-- 添加表注释
COMMENT ON TABLE ai_model IS 'AI模型表';

-- 添加列注释
COMMENT ON COLUMN ai_model.id IS '主键ID';
COMMENT ON COLUMN ai_model.model_name IS '模型名称';
COMMENT ON COLUMN ai_model.model_identifier IS '模型标识符(唯一)';
COMMENT ON COLUMN ai_model.access_type IS '接入方式(remote_api/local)';
COMMENT ON COLUMN ai_model.model_type IS '模型类型(llm/embedding/sentiment_classification等)';
COMMENT ON COLUMN ai_model.model_source IS '模型来源(remote_api/code_defined)';
COMMENT ON COLUMN ai_model.provider_id IS '提供商ID(远程API时必填)';
COMMENT ON COLUMN ai_model.model_config IS '模型配置(JSON格式)';
COMMENT ON COLUMN ai_model.description IS '描述信息';
COMMENT ON COLUMN ai_model.is_enabled IS '是否启用';
COMMENT ON COLUMN ai_model.create_time IS '创建时间';
COMMENT ON COLUMN ai_model.update_time IS '更新时间';
COMMENT ON COLUMN ai_model.create_by IS '创建人ID';
COMMENT ON COLUMN ai_model.update_by IS '更新人ID';
COMMENT ON COLUMN ai_model.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';
