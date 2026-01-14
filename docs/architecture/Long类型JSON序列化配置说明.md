# Long 类型 JSON 序列化配置说明

## 1. 问题背景

### 1.1 JavaScript 精度丢失问题

JavaScript 中的 Number 类型使用 IEEE 754 双精度浮点数表示，安全整数范围为：

```
-9007199254740991 到 9007199254740991
即：-(2^53 - 1) 到 (2^53 - 1)
```

超出此范围的整数会丢失精度。

### 1.2 雪花算法 ID 问题

雪花算法生成的 ID 是 64 位 Long 类型：

```
范围：-9223372036854775808 到 9223372036854775807
即：-(2^63) 到 (2^63 - 1)
```

**远超 JavaScript 安全整数范围**，直接传递会导致精度丢失。

### 1.3 示例

```javascript
// 后端返回的 Long 值
const id = 1234567890123456789;

// JavaScript 实际存储的值（精度丢失）
console.log(id); // 1234567890123456800

// 精度丢失了 11
```

## 2. 解决方案

### 2.1 配置说明

已创建 `JacksonConfig` 配置类，自动将以下类型序列化为 String：

| Java 类型 | 序列化后类型 | 说明 |
|-----------|-------------|------|
| `Long` | `String` | 包括包装类型和基本类型 |
| `BigInteger` | `String` | 大整数类型 |

### 2.2 配置文件

📄 [JacksonConfig.java](src/main/java/org/joker/comfypilot/common/config/JacksonConfig.java)

核心配置代码：

```java
SimpleModule simpleModule = new SimpleModule();

// Long 类型序列化为 String
simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

// BigInteger 类型序列化为 String
simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);

objectMapper.registerModule(simpleModule);
```

## 3. 使用示例

### 3.1 后端代码

```java
@Data
public class WorkflowDTO {
    private Long id;              // 会序列化为 String
    private Long userId;          // 会序列化为 String
    private String name;          // 保持 String
    private Integer status;       // 保持 Integer
    private LocalDateTime createdAt; // 序列化为 "yyyy-MM-dd HH:mm:ss"
}
```

### 3.2 JSON 响应示例

**后端返回：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": "1234567890123456789",
    "userId": "9876543210987654321",
    "name": "测试工作流",
    "status": 1,
    "createdAt": "2026-01-14 20:30:45"
  },
  "traceId": "abc123def456",
  "timestamp": 1705234245123
}
```

**注意**：
- `id` 和 `userId` 是 **字符串类型**（带引号）
- `status` 是 **数字类型**（不带引号）
- `timestamp` 也会序列化为字符串

### 3.3 前端处理

```typescript
interface WorkflowDTO {
  id: string;           // 注意：类型是 string
  userId: string;       // 注意：类型是 string
  name: string;
  status: number;
  createdAt: string;
}

// 使用示例
const workflow: WorkflowDTO = await api.getWorkflow(id);
console.log(workflow.id);  // "1234567890123456789"

// 如果需要比较 ID
if (workflow.id === "1234567890123456789") {
  // 字符串比较，不会丢失精度
}
```

## 4. 测试验证

### 4.1 测试接口

已创建测试接口：

```
GET /api/v1/test/json-serialization
```

### 4.2 测试步骤

1. 启动应用
2. 访问测试接口：
   ```bash
   curl http://localhost:8080/api/v1/test/json-serialization
   ```

3. 查看响应：
   ```json
   {
     "code": 200,
     "message": "操作成功",
     "data": {
       "id": "1234567890123456789",
       "userId": "9876543210987654321",
       "name": "测试数据",
       "createdAt": "2026-01-14 20:30:45"
     },
     "traceId": "abc123def456",
     "timestamp": "1705234245123"
   }
   ```

### 4.3 验证要点

✅ `id` 和 `userId` 是字符串（带引号）
✅ 数值完整，无精度丢失
✅ `timestamp` 也是字符串

## 5. 日期时间格式配置

### 5.1 配置的格式

| Java 类型 | 格式 | 示例 |
|-----------|------|------|
| `LocalDateTime` | `yyyy-MM-dd HH:mm:ss` | `2026-01-14 20:30:45` |
| `LocalDate` | `yyyy-MM-dd` | `2026-01-14` |
| `LocalTime` | `HH:mm:ss` | `20:30:45` |

### 5.2 示例

```java
@Data
public class EventDTO {
    private Long id;
    private LocalDateTime eventTime;    // "2026-01-14 20:30:45"
    private LocalDate eventDate;        // "2026-01-14"
    private LocalTime eventTimeOnly;    // "20:30:45"
}
```

## 6. 注意事项

### 6.1 数据库设计

✅ **推荐做法**：

```sql
CREATE TABLE workflow (
    id BIGINT PRIMARY KEY,           -- 使用 BIGINT
    user_id BIGINT NOT NULL,         -- 外键字段也用 BIGINT
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 不使用外键约束，在代码层面控制
-- FOREIGN KEY (user_id) REFERENCES user(id)  ❌ 不推荐
```

### 6.2 MyBatis-Plus 配置

已在 `application.yml` 中配置：

```yaml
mybatis-plus:
  global-config:
    db-config:
      # 主键类型（雪花算法）
      id-type: ASSIGN_ID
```

### 6.3 实体类定义

```java
@Data
@TableName("workflow")
public class WorkflowPO extends BasePO {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;  // 自动使用雪花算法生成

    private Long userId;  // 外键字段，代码层面控制约束
}
```

### 6.4 外键约束处理

**在代码层面控制外键语义**：

```java
@Service
public class WorkflowApplicationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkflowMapper workflowMapper;

    public void createWorkflow(WorkflowDTO dto) {
        // 手动检查外键约束
        if (userMapper.selectById(dto.getUserId()) == null) {
            throw new ValidationException("用户不存在");
        }

        // 保存工作流
        WorkflowPO po = new WorkflowPO();
        po.setUserId(dto.getUserId());
        workflowMapper.insert(po);
    }
}
```

## 7. 前端最佳实践

### 7.1 TypeScript 类型定义

```typescript
// 基础 DTO 类型
interface BaseDTO {
  id: string;           // Long 类型序列化为 string
  createdAt: string;    // LocalDateTime 序列化为 string
  updatedAt: string;
}

// 工作流 DTO
interface WorkflowDTO extends BaseDTO {
  name: string;
  userId: string;       // Long 类型序列化为 string
  status: number;
}
```

### 7.2 ID 比较

```typescript
// ✅ 正确：字符串比较
if (workflow.id === "1234567890123456789") {
  // ...
}

// ❌ 错误：不要转换为数字
if (Number(workflow.id) === 1234567890123456789) {
  // 可能丢失精度
}
```

### 7.3 ID 传递

```typescript
// ✅ 正确：直接传递字符串
await api.deleteWorkflow(workflow.id);

// 请求参数
const params = {
  workflowId: workflow.id  // 字符串类型
};
```

## 8. 常见问题

### Q1: 为什么不在前端处理？

**A**: 后端统一处理更可靠：
- 避免前端遗漏处理
- 统一序列化规则
- 减少前端代码复杂度

### Q2: Integer 类型会转换吗？

**A**: 不会。只有 Long 和 BigInteger 会转换为 String。

```json
{
  "id": "1234567890123456789",  // Long → String
  "status": 1,                   // Integer → Integer
  "count": 100                   // Integer → Integer
}
```

### Q3: 如何在特定字段禁用转换？

**A**: 使用 `@JsonSerialize` 注解：

```java
@Data
public class SpecialDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;  // 转换为 String

    @JsonSerialize(using = JsonSerializer.None.class)
    private Long rawId;  // 保持 Long（不推荐）
}
```

### Q4: 反序列化时如何处理？

**A**: 前端传递字符串，后端自动转换为 Long：

```java
// 前端传递
{
  "userId": "1234567890123456789"
}

// 后端接收
@PostMapping
public Result<Void> create(@RequestBody WorkflowDTO dto) {
    Long userId = dto.getUserId();  // 自动转换为 Long
}
```

## 9. 总结

### 9.1 配置效果

✅ **Long 类型自动序列化为 String**
✅ **避免前端精度丢失**
✅ **日期时间格式统一**
✅ **全局生效，无需额外配置**

### 9.2 开发规范

1. **数据库**：主键和外键字段使用 `BIGINT`
2. **Java**：使用 `Long` 类型
3. **前端**：使用 `string` 类型
4. **外键约束**：代码层面控制，不使用数据库外键

---

**文档版本**：v1.0
**创建日期**：2026-01-14
**维护者**：开发团队
