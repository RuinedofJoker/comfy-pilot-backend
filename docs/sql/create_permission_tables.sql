-- 权限模块数据库迁移脚本
-- 创建时间: 2026-01-16

-- 1. 创建角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    is_deleted BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_role_code UNIQUE (role_code)
);

COMMENT ON TABLE role IS '角色表';
COMMENT ON COLUMN role.id IS '主键ID';
COMMENT ON COLUMN role.role_code IS '角色编码（唯一）';
COMMENT ON COLUMN role.role_name IS '角色名称';
COMMENT ON COLUMN role.description IS '角色描述';
COMMENT ON COLUMN role.is_system IS '是否系统内置角色';
COMMENT ON COLUMN role.create_time IS '创建时间';
COMMENT ON COLUMN role.create_by IS '创建人ID';
COMMENT ON COLUMN role.update_time IS '更新时间';
COMMENT ON COLUMN role.update_by IS '更新人ID';
COMMENT ON COLUMN role.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- 2. 创建权限表
CREATE TABLE IF NOT EXISTS permission (
    id BIGINT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    is_deleted BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_permission_code UNIQUE (permission_code)
);

COMMENT ON TABLE permission IS '权限表';
COMMENT ON COLUMN permission.id IS '主键ID';
COMMENT ON COLUMN permission.permission_code IS '权限编码（唯一）';
COMMENT ON COLUMN permission.permission_name IS '权限名称';
COMMENT ON COLUMN permission.resource_type IS '资源类型';
COMMENT ON COLUMN permission.description IS '权限描述';
COMMENT ON COLUMN permission.create_time IS '创建时间';
COMMENT ON COLUMN permission.create_by IS '创建人ID';
COMMENT ON COLUMN permission.update_time IS '更新时间';
COMMENT ON COLUMN permission.update_by IS '更新人ID';
COMMENT ON COLUMN permission.is_deleted IS '逻辑删除标记（0-未删除，非0-删除时的时间戳）';

-- 3. 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permission (
    id BIGINT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_permission UNIQUE (role_id, permission_id)
);

COMMENT ON TABLE role_permission IS '角色权限关联表';
COMMENT ON COLUMN role_permission.id IS '主键ID';
COMMENT ON COLUMN role_permission.role_id IS '角色ID';
COMMENT ON COLUMN role_permission.permission_id IS '权限ID';
COMMENT ON COLUMN role_permission.create_time IS '创建时间';

-- 4. 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_role (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role UNIQUE (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON user_role(user_id);

COMMENT ON TABLE user_role IS '用户角色关联表';
COMMENT ON COLUMN user_role.id IS '主键ID';
COMMENT ON COLUMN user_role.user_id IS '用户ID';
COMMENT ON COLUMN user_role.role_id IS '角色ID';
COMMENT ON COLUMN user_role.create_time IS '创建时间';
