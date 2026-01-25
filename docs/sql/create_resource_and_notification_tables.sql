-- =============================================
-- 资源模块和通知模块数据表
-- =============================================

-- =============================================
-- 文件资源表
-- =============================================
CREATE TABLE file_resource (
    id BIGINT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    file_extension VARCHAR(50),
    source_type VARCHAR(50) NOT NULL,
    web_relative_path VARCHAR(500) NOT NULL,
    upload_user_id BIGINT NOT NULL,
    business_type VARCHAR(50),
    business_id BIGINT,
    download_count INT DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL,
    is_deleted BIGINT DEFAULT 0
);

-- 文件资源表索引
CREATE INDEX idx_file_resource_upload_user_id ON file_resource(upload_user_id);
CREATE INDEX idx_file_resource_business ON file_resource(business_type, business_id);
CREATE UNIQUE INDEX uk_file_resource_sources_stored_name ON file_resource(source_type, stored_name, is_deleted);
CREATE INDEX idx_file_resource_create_time ON file_resource(create_time);

-- 文件资源表注释
COMMENT ON TABLE file_resource IS '文件资源表';
COMMENT ON COLUMN file_resource.id IS '主键ID';
COMMENT ON COLUMN file_resource.file_name IS '原始文件名';
COMMENT ON COLUMN file_resource.stored_name IS '存储文件名（唯一）';
COMMENT ON COLUMN file_resource.file_path IS '文件存储路径';
COMMENT ON COLUMN file_resource.file_size IS '文件大小（字节）';
COMMENT ON COLUMN file_resource.file_type IS '文件MIME类型';
COMMENT ON COLUMN file_resource.file_extension IS '文件扩展名';
COMMENT ON COLUMN file_resource.upload_user_id IS '上传用户ID';
COMMENT ON COLUMN file_resource.business_type IS '业务类型（workflow/avatar等）';
COMMENT ON COLUMN file_resource.business_id IS '业务关联ID';
COMMENT ON COLUMN file_resource.download_count IS '下载次数';
COMMENT ON COLUMN file_resource.create_time IS '创建时间';
COMMENT ON COLUMN file_resource.create_by IS '创建人ID';
COMMENT ON COLUMN file_resource.update_time IS '更新时间';
COMMENT ON COLUMN file_resource.update_by IS '更新人ID';
COMMENT ON COLUMN file_resource.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- =============================================
-- 邮件发送日志表
-- =============================================
CREATE TABLE email_log (
    id BIGINT PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    send_status VARCHAR(50) NOT NULL,
    error_message TEXT,
    send_time TIMESTAMP,
    retry_count INT DEFAULT 0,
    business_type VARCHAR(50),
    business_id VARCHAR(100),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT NOT NULL,
    is_deleted BIGINT DEFAULT 0
);

-- 邮件日志表索引
CREATE INDEX idx_email_log_recipient ON email_log(recipient);
CREATE INDEX idx_email_log_send_status ON email_log(send_status);
CREATE INDEX idx_email_log_business ON email_log(business_type, business_id);
CREATE INDEX idx_email_log_create_time ON email_log(create_time);

-- 邮件日志表注释
COMMENT ON TABLE email_log IS '邮件发送日志表';
COMMENT ON COLUMN email_log.id IS '主键ID';
COMMENT ON COLUMN email_log.recipient IS '收件人邮箱';
COMMENT ON COLUMN email_log.subject IS '邮件主题';
COMMENT ON COLUMN email_log.content IS '邮件内容';
COMMENT ON COLUMN email_log.send_status IS '发送状态（PENDING/SUCCESS/FAILED）';
COMMENT ON COLUMN email_log.error_message IS '错误信息';
COMMENT ON COLUMN email_log.send_time IS '实际发送时间';
COMMENT ON COLUMN email_log.retry_count IS '重试次数';
COMMENT ON COLUMN email_log.business_type IS '业务类型（PASSWORD_RESET/REGISTER等）';
COMMENT ON COLUMN email_log.business_id IS '业务关联ID';
COMMENT ON COLUMN email_log.create_time IS '创建时间';
COMMENT ON COLUMN email_log.create_by IS '创建人ID';
COMMENT ON COLUMN email_log.update_time IS '更新时间';
COMMENT ON COLUMN email_log.update_by IS '更新人ID';
COMMENT ON COLUMN email_log.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';
