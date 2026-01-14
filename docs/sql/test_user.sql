-- 测试用户表
-- 用于测试 MyBatis-Plus 和数据库连接功能

DROP TABLE IF EXISTS test_user;

CREATE TABLE test_user (
    id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    age INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

-- 创建索引
CREATE INDEX idx_username ON test_user(username);
CREATE INDEX idx_deleted ON test_user(deleted);

-- 添加表注释
COMMENT ON TABLE test_user IS '测试用户表';

-- 添加列注释
COMMENT ON COLUMN test_user.id IS '主键ID';
COMMENT ON COLUMN test_user.username IS '用户名';
COMMENT ON COLUMN test_user.email IS '邮箱';
COMMENT ON COLUMN test_user.age IS '年龄';
COMMENT ON COLUMN test_user.created_at IS '创建时间';
COMMENT ON COLUMN test_user.updated_at IS '更新时间';
COMMENT ON COLUMN test_user.created_by IS '创建人ID';
COMMENT ON COLUMN test_user.updated_by IS '更新人ID';
COMMENT ON COLUMN test_user.deleted IS '逻辑删除标记(0:未删除,1:已删除)';

-- 插入测试数据
INSERT INTO test_user (id, username, email, age, created_at, updated_at) VALUES
(1, '张三', 'zhangsan@example.com', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '李四', 'lisi@example.com', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '王五', 'wangwu@example.com', 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
