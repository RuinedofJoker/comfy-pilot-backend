-- =====================================================
-- V7: 创建AI模型模块相关表
-- 创建时间: 2026-01-16
-- 说明: 支持远程API/本地接入 × LLM/Embedding等模型类型
-- =====================================================

-- =====================================================
-- 1. model_provider - 模型提供商表
-- =====================================================
CREATE TABLE IF NOT EXISTS model_provider (
    id BIGINT PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL COMMENT '提供商名称',
    provider_type VARCHAR(50) NOT NULL COMMENT '提供商类型(openai/anthropic/aliyun/custom)',
    api_base_url VARCHAR(500) COMMENT 'API基础URL',
    description TEXT COMMENT '描述信息',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID'
);

-- 创建索引
CREATE INDEX idx_provider_type ON model_provider(provider_type);
CREATE INDEX idx_provider_enabled ON model_provider(is_enabled);

-- 添加注释
COMMENT ON TABLE model_provider IS '模型提供商表';

-- =====================================================
-- 2. ai_model - AI模型表
-- =====================================================
CREATE TABLE IF NOT EXISTS ai_model (
    id BIGINT PRIMARY KEY,
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
    model_identifier VARCHAR(100) NOT NULL UNIQUE COMMENT '模型标识符(唯一)',
    access_type VARCHAR(50) NOT NULL COMMENT '接入方式(remote_api/local)',
    model_type VARCHAR(50) NOT NULL COMMENT '模型类型(llm/embedding/sentiment_classification等)',
    model_source VARCHAR(50) NOT NULL DEFAULT 'remote_api' COMMENT '模型来源(remote_api/code_defined)',
    provider_id BIGINT COMMENT '提供商ID(远程API时必填)',
    model_config TEXT COMMENT '模型配置(JSON格式)',
    description TEXT COMMENT '描述信息',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    CONSTRAINT fk_ai_model_provider FOREIGN KEY (provider_id) REFERENCES model_provider(id)
);

-- 创建索引
CREATE UNIQUE INDEX uk_model_identifier ON ai_model(model_identifier);
CREATE INDEX idx_model_access_type ON ai_model(access_type);
CREATE INDEX idx_model_type ON ai_model(model_type);
CREATE INDEX idx_model_source ON ai_model(model_source);
CREATE INDEX idx_model_provider_id ON ai_model(provider_id);
CREATE INDEX idx_model_enabled ON ai_model(is_enabled);

-- 添加注释
COMMENT ON TABLE ai_model IS 'AI模型表';

-- =====================================================
-- 3. model_api_key - 模型API密钥表
-- =====================================================
CREATE TABLE IF NOT EXISTS model_api_key (
    id BIGINT PRIMARY KEY,
    provider_id BIGINT NOT NULL COMMENT '提供商ID',
    key_name VARCHAR(100) NOT NULL COMMENT '密钥名称',
    api_key VARCHAR(500) NOT NULL COMMENT 'API密钥(加密存储)',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    CONSTRAINT fk_model_api_key_provider FOREIGN KEY (provider_id) REFERENCES model_provider(id)
);

-- 创建索引
CREATE INDEX idx_api_key_provider_id ON model_api_key(provider_id);
CREATE INDEX idx_api_key_enabled ON model_api_key(is_enabled);

-- 添加注释
COMMENT ON TABLE model_api_key IS '模型API密钥表';

