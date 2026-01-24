-- =====================================================
-- 工作流模块数据库迁移脚本
-- 版本: V6
-- 描述: 创建workflow和workflow_version表
-- 作者: System
-- 日期: 2026-01-16
-- =====================================================

-- =====================================================
-- 1. 创建workflow表（工作流表）
-- =====================================================
CREATE TABLE workflow (
    id BIGINT PRIMARY KEY,
    workflow_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    comfyui_server_id BIGINT NOT NULL,
    comfyui_server_key VARCHAR(100) NOT NULL,
    active_content TEXT,
    active_content_hash VARCHAR(64),
    thumbnail_url VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL,
    is_deleted BIGINT NOT NULL DEFAULT 0
);

-- 添加表注释
COMMENT ON TABLE workflow IS '工作流表';
COMMENT ON COLUMN workflow.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN workflow.workflow_name IS '工作流名称';
COMMENT ON COLUMN workflow.description IS '工作流描述';
COMMENT ON COLUMN workflow.comfyui_server_id IS '所属ComfyUI服务ID';
COMMENT ON COLUMN workflow.comfyui_server_key IS '所属ComfyUI服务唯一标识符';
COMMENT ON COLUMN workflow.active_content IS '当前激活版本的内容（JSON格式）';
COMMENT ON COLUMN workflow.active_content_hash IS '激活内容的SHA-256哈希值';
COMMENT ON COLUMN workflow.thumbnail_url IS '工作流缩略图URL';
COMMENT ON COLUMN workflow.create_time IS '创建时间';
COMMENT ON COLUMN workflow.create_by IS '创建人ID';
COMMENT ON COLUMN workflow.update_time IS '更新时间';
COMMENT ON COLUMN workflow.update_by IS '更新人ID';
COMMENT ON COLUMN workflow.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- =====================================================
-- 2. 创建workflow表的索引
-- =====================================================
CREATE INDEX idx_workflow_server_id ON workflow(comfyui_server_id);
CREATE INDEX idx_workflow_server_key ON workflow(comfyui_server_key);
CREATE INDEX idx_workflow_create_by ON workflow(create_by);

-- =====================================================
-- 3. 创建workflow_version表（工作流版本表）
-- =====================================================
CREATE TABLE workflow_version (
    id BIGINT PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    version_code VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    change_summary VARCHAR(500),
    session_id BIGINT,
    message_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT NOT NULL,
    is_deleted BIGINT NOT NULL DEFAULT 0
);

-- 添加表注释
COMMENT ON TABLE workflow_version IS '工作流版本表';
COMMENT ON COLUMN workflow_version.id IS '主键ID（雪花算法）';
COMMENT ON COLUMN workflow_version.workflow_id IS '所属工作流ID';
COMMENT ON COLUMN workflow_version.version_code IS '版本号（UUID）';
COMMENT ON COLUMN workflow_version.content IS '版本内容（JSON格式）';
COMMENT ON COLUMN workflow_version.content_hash IS '内容的SHA-256哈希值';
COMMENT ON COLUMN workflow_version.change_summary IS '变更摘要（Agent生成）';
COMMENT ON COLUMN workflow_version.session_id IS '关联的会话ID（如果是Agent对话生成）';
COMMENT ON COLUMN workflow_version.message_id IS '关联的会话消息ID（如果是Agent对话生成）';
COMMENT ON COLUMN workflow_version.create_time IS '创建时间';
COMMENT ON COLUMN workflow_version.create_by IS '创建人ID';
COMMENT ON COLUMN workflow_version.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- =====================================================
-- 4. 创建workflow_version表的索引
-- =====================================================
CREATE UNIQUE INDEX uk_workflow_version ON workflow_version(workflow_id, version_code, is_deleted);
CREATE INDEX idx_version_workflow_id ON workflow_version(workflow_id);
CREATE INDEX idx_version_content_hash ON workflow_version(content_hash);
CREATE INDEX idx_version_session_id ON workflow_version(session_id);
CREATE INDEX idx_version_message_id ON workflow_version(message_id);
CREATE INDEX idx_version_create_time ON workflow_version(create_time);
