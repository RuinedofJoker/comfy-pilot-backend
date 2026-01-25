-- 用户模块数据库迁移脚本
-- 创建时间: 2026-01-16

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS "user" (
    id BIGINT PRIMARY KEY,
    user_code VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_login_time TIMESTAMP,
    last_login_ip VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    is_deleted BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_code UNIQUE (user_code),
    CONSTRAINT uk_user_email UNIQUE (email)
);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON COLUMN "user".id IS '主键ID（雪花算法）';
COMMENT ON COLUMN "user".user_code IS '用户编码（唯一）';
COMMENT ON COLUMN "user".email IS '邮箱地址（唯一）';
COMMENT ON COLUMN "user".username IS '用户名';
COMMENT ON COLUMN "user".password_hash IS '密码哈希值（BCrypt）';
COMMENT ON COLUMN "user".avatar_url IS '头像URL';
COMMENT ON COLUMN "user".status IS '用户状态（ACTIVE-活跃，INACTIVE-未激活，LOCKED-锁定，DELETED-已删除）';
COMMENT ON COLUMN "user".last_login_time IS '最后登录时间';
COMMENT ON COLUMN "user".last_login_ip IS '最后登录IP';
COMMENT ON COLUMN "user".create_time IS '创建时间';
COMMENT ON COLUMN "user".create_by IS '创建人ID';
COMMENT ON COLUMN "user".update_time IS '更新时间';
COMMENT ON COLUMN "user".update_by IS '更新人ID';
COMMENT ON COLUMN "user".is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- 2. 创建索引
CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email);
CREATE INDEX IF NOT EXISTS idx_user_status ON "user"(status);
