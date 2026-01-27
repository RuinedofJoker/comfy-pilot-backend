-- =====================================================
-- V9: 创建会话模块相关表
-- 创建时间: 2026-01-17
-- 说明: 支持聊天会话管理和消息历史记录
-- =====================================================

-- =====================================================
-- 1. chat_session - 聊天会话表
-- =====================================================
CREATE TABLE IF NOT EXISTS chat_session (
    id BIGINT PRIMARY KEY,
    session_code VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL,
    comfyui_server_id BIGINT NOT NULL,
    title VARCHAR(200),
    rules TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    is_deleted INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 创建唯一索引
CREATE UNIQUE INDEX uk_session_code ON chat_session(session_code, is_deleted);

-- 创建普通索引
CREATE INDEX idx_chat_session_user_id ON chat_session(user_id);
CREATE INDEX idx_chat_session_comfyui_server_id ON chat_session(comfyui_server_id);
CREATE INDEX idx_chat_session_status ON chat_session(status);
CREATE INDEX idx_chat_session_create_time ON chat_session(create_time);
CREATE INDEX idx_chat_session_is_deleted ON chat_session(is_deleted);

-- 创建复合索引
CREATE INDEX idx_chat_session_user_status ON chat_session(user_id, status);

-- 添加注释
COMMENT ON TABLE chat_session IS '聊天会话表';

-- =====================================================
-- 2. chat_message - 聊天消息表
-- =====================================================
CREATE TABLE IF NOT EXISTS chat_message (
    id BIGINT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    session_code VARCHAR(50) NOT NULL,
    request_id VARCHAR(50) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    content TEXT NOT NULL,
    chat_content TEXT,
    metadata TEXT,
    is_deleted BIGINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT
);

-- 创建普通索引
CREATE INDEX idx_chat_message_session_id ON chat_message(session_id);
CREATE INDEX idx_chat_message_session_code ON chat_message(session_code);
CREATE INDEX idx_chat_message_request_id ON chat_message(request_id);
CREATE INDEX idx_chat_message_role ON chat_message(role);
CREATE INDEX idx_chat_message_status ON chat_message(status);
CREATE INDEX idx_chat_message_create_time ON chat_message(create_time);
CREATE INDEX idx_chat_message_is_deleted ON chat_message(is_deleted);

-- 创建复合索引
CREATE INDEX idx_chat_message_session_id_request_create ON chat_message(session_id, request_id, create_time);
CREATE INDEX idx_chat_message_session_code_request_create ON chat_message(session_code, request_id, create_time);
CREATE INDEX idx_chat_message_session_role ON chat_message(session_id, role);
CREATE INDEX idx_chat_message_session_status ON chat_message(session_id, status);

-- 添加注释
COMMENT ON TABLE chat_message IS '聊天消息表';
