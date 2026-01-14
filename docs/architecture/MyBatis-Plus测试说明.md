# MyBatis-Plus 数据库功能测试

## 1. 测试表 SQL

📄 [test_user.sql](../sql/test_user.sql)

**PostgreSQL 版本**:

```sql
-- 创建测试表
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

-- 添加注释
COMMENT ON TABLE test_user IS '测试用户表';
COMMENT ON COLUMN test_user.id IS '主键ID';
COMMENT ON COLUMN test_user.username IS '用户名';
COMMENT ON COLUMN test_user.email IS '邮箱';
COMMENT ON COLUMN test_user.age IS '年龄';
COMMENT ON COLUMN test_user.created_at IS '创建时间';
COMMENT ON COLUMN test_user.updated_at IS '更新时间';

-- 插入测试数据
INSERT INTO test_user (id, username, email, age, created_at, updated_at) VALUES
(1, '张三', 'zhangsan@example.com', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '李四', 'lisi@example.com', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, '王五', 'wangwu@example.com', 28, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
```

**重要说明**:

✅ **字段自动填充由 MyBatis-Plus 处理**
- `created_at`、`updated_at`、`created_by`、`updated_by` 字段由 [MybatisPlusMetaObjectHandler](../../src/main/java/org/joker/comfypilot/common/config/MybatisPlusMetaObjectHandler.java) 自动填充
- 数据库表中这些字段**不设置** `DEFAULT` 值和触发器
- 插入/更新操作时，MyBatis-Plus 会自动设置这些字段的值

**MySQL 与 PostgreSQL 差异说明**:

| 特性 | MySQL | PostgreSQL |
|------|-------|-----------|
| 标识符引用 | 反引号 `` ` `` | 双引号 `"` 或不使用 |
| 日期时间类型 | `DATETIME` | `TIMESTAMP` |
| 小整数类型 | `TINYINT` | `SMALLINT` |
| 注释语法 | `COMMENT '...'` | `COMMENT ON ... IS '...'` |
| 存储引擎 | `ENGINE=InnoDB` | 不需要指定 |
| 字符集 | `CHARSET=utf8mb4` | 不需要指定 |

## 2. 代码结构

```
test/
├── application/
│   ├── dto/
│   │   └── TestUserDTO.java          # DTO 数据传输对象
│   └── service/
│       └── TestUserService.java      # Service 业务逻辑层
├── infrastructure/
│   └── persistence/
│       ├── mapper/
│       │   └── TestUserMapper.java   # Mapper 数据访问层
│       └── po/
│           └── TestUserPO.java       # PO 持久化对象
└── interfaces/
    └── controller/
        └── TestUserController.java   # Controller 控制器层
```

## 3. 测试步骤

### 3.1 执行 SQL

在数据库中执行 [test_user.sql](../sql/test_user.sql) 创建表并插入测试数据。

### 3.2 启动应用

```bash
mvn spring-boot:run
```

### 3.3 测试接口

#### 测试 1: 查询所有用户

```bash
curl http://localhost:8080/api/v1/test/users
```

**预期响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "1",
      "username": "张三",
      "email": "zhangsan@example.com",
      "age": 25,
      "createdAt": "2026-01-14 20:30:45",
      "updatedAt": "2026-01-14 20:30:45"
    },
    {
      "id": "2",
      "username": "李四",
      "email": "lisi@example.com",
      "age": 30,
      "createdAt": "2026-01-14 20:30:45",
      "updatedAt": "2026-01-14 20:30:45"
    },
    {
      "id": "3",
      "username": "王五",
      "email": "wangwu@example.com",
      "age": 28,
      "createdAt": "2026-01-14 20:30:45",
      "updatedAt": "2026-01-14 20:30:45"
    }
  ],
  "traceId": "abc123def456",
  "timestamp": "1705234245123"
}
```

#### 测试 2: 根据 ID 查询用户

```bash
curl http://localhost:8080/api/v1/test/users/1
```

**预期响应**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1",
    "username": "张三",
    "email": "zhangsan@example.com",
    "age": 25,
    "createdAt": "2026-01-14 20:30:45",
    "updatedAt": "2026-01-14 20:30:45"
  },
  "traceId": "abc123def456",
  "timestamp": "1705234245123"
}
```

#### 测试 3: 创建用户

```bash
curl -X POST http://localhost:8080/api/v1/test/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "赵六",
    "email": "zhaoliu@example.com",
    "age": 35
  }'
```

**预期响应**:

```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": "1234567890123456789",
    "username": "赵六",
    "email": "zhaoliu@example.com",
    "age": 35,
    "createdAt": "2026-01-14 20:35:12",
    "updatedAt": "2026-01-14 20:35:12"
  },
  "traceId": "abc123def456",
  "timestamp": "1705234512123"
}
```

## 4. 验证要点

### 4.1 MyBatis-Plus 功能

✅ **BaseMapper 方法**: `selectList`, `selectById`, `insert`
✅ **雪花算法 ID**: 新创建的用户 ID 是 Long 类型（由 `@TableId(type = IdType.ASSIGN_ID)` 配置）
✅ **自动填充**: `createdAt`, `updatedAt`, `createdBy`, `updatedBy` 由 [MybatisPlusMetaObjectHandler](../../src/main/java/org/joker/comfypilot/common/config/MybatisPlusMetaObjectHandler.java) 自动填充
✅ **逻辑删除**: `deleted` 字段由 `@TableLogic` 注解自动处理

**自动填充机制说明**:

| 字段 | 插入时填充 | 更新时填充 | 填充值 |
|------|-----------|-----------|--------|
| `id` | ✅ | ❌ | 雪花算法生成的 Long 值 |
| `createdAt` | ✅ | ❌ | `LocalDateTime.now()` |
| `updatedAt` | ✅ | ✅ | `LocalDateTime.now()` |
| `createdBy` | ✅ | ❌ | 当前用户 ID（TODO: 从上下文获取） |
| `updatedBy` | ✅ | ✅ | 当前用户 ID（TODO: 从上下文获取） |

**配置文件位置**:
- 📄 [BasePO.java](../../src/main/java/org/joker/comfypilot/common/infrastructure/persistence/po/BasePO.java) - 基类定义字段和注解
- 📄 [MybatisPlusMetaObjectHandler.java](../../src/main/java/org/joker/comfypilot/common/config/MybatisPlusMetaObjectHandler.java) - 自动填充处理器

### 4.2 JSON 序列化

✅ **Long 转 String**: ID 字段序列化为字符串
✅ **日期格式**: `createdAt` 格式为 `yyyy-MM-dd HH:mm:ss`
✅ **统一响应**: 使用 `Result` 包装响应

### 4.3 TraceId

✅ **自动注入**: 每个请求自动生成 TraceId
✅ **响应包含**: 响应中包含 `traceId` 字段
✅ **日志记录**: 日志中包含 TraceId

## 5. 常见问题

### Q1: 表不存在

**错误**: `Table 'database.test_user' doesn't exist`

**解决**: 执行 [test_user.sql](../sql/test_user.sql) 创建表

### Q2: Mapper 找不到

**错误**: `No qualifying bean of type 'TestUserMapper'`

**解决**: 确保 Mapper 接口有 `@Mapper` 注解

### Q3: ID 为 null

**错误**: 创建用户后 ID 为 null

**解决**: 检查 `BasePO` 中的 `@TableId(type = IdType.ASSIGN_ID)` 配置

### Q4: 时间格式错误

**错误**: 时间显示为时间戳

**解决**: 检查 [JacksonConfig](../../src/main/java/org/joker/comfypilot/common/config/JacksonConfig.java) 配置

## 6. 清理测试数据

测试完成后,可以删除测试表:

```sql
DROP TABLE IF EXISTS `test_user`;
```

---

**文档版本**: v1.0
**创建日期**: 2026-01-14
**维护者**: 开发团队
