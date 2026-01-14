# Redis 配置说明

## 1. 概述

本项目使用 **Fastjson2** 作为 Redis 的序列化和反序列化器,用于 String 类型的 value 和 Hash 类型的 value。

## 2. 配置文件

### 2.1 核心配置类

| 配置类 | 路径 | 说明 |
|--------|------|------|
| **RedisConfig** | `common.config.RedisConfig` | Redis 序列化配置 |
| **Fastjson2Config** | `common.config.Fastjson2Config` | Fastjson2 全局特性配置 |
| **RedisUtil** | `common.util.RedisUtil` | Redis 工具类 |

### 2.2 RedisConfig 配置

📄 [RedisConfig.java](src/main/java/org/joker/comfypilot/common/config/RedisConfig.java)

**核心配置**:

```java
@Bean
public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(factory);

    // Key 使用 String 序列化
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);

    // Value 使用 Fastjson2 序列化
    GenericFastJsonRedisSerializer fastJsonSerializer = new GenericFastJsonRedisSerializer();
    template.setValueSerializer(fastJsonSerializer);
    template.setHashValueSerializer(fastJsonSerializer);

    template.afterPropertiesSet();
    return template;
}
```

**序列化器说明**:

| 数据类型 | Key 序列化器 | Value 序列化器 |
|---------|-------------|---------------|
| String | `StringRedisSerializer` | `GenericFastJsonRedisSerializer` |
| Hash | `StringRedisSerializer` | `GenericFastJsonRedisSerializer` |

### 2.3 Fastjson2Config 配置

📄 [Fastjson2Config.java](src/main/java/org/joker/comfypilot/common/config/Fastjson2Config.java)

**解决 Fastjson2 反序列化访问问题**:

```java
@PostConstruct
public void init() {
    // 支持自动类型转换
    JSON.config(JSONReader.Feature.SupportAutoType);

    // 序列化时写入类型信息
    JSON.config(JSONWriter.Feature.WriteClassName);

    // 基于字段反序列化(允许访问私有字段)
    JSON.config(JSONReader.Feature.FieldBased);

    // 支持数组转 Bean
    JSON.config(JSONReader.Feature.SupportArrayToBean);
}
```

**特性说明**:

| 特性 | 说明 | 解决的问题 |
|------|------|-----------|
| `SupportAutoType` | 支持自动类型转换 | 允许反序列化时自动识别类型 |
| `WriteClassName` | 写入类型信息 | 序列化时保存类型,反序列化时恢复 |
| `FieldBased` | 基于字段反序列化 | 允许访问私有字段,无需 getter/setter |
| `SupportArrayToBean` | 支持数组转 Bean | 支持数组格式的 JSON 转换为对象 |

## 3. RedisUtil 工具类

📄 [RedisUtil.java](src/main/java/org/joker/comfypilot/common/util/RedisUtil.java)

### 3.1 功能清单

| 操作类型 | 方法 | 说明 |
|---------|------|------|
| **通用操作** | `expire` | 设置过期时间 |
| | `getExpire` | 获取过期时间 |
| | `hasKey` | 判断 key 是否存在 |
| | `del` | 删除 key |
| **String 操作** | `get` | 获取值 |
| | `set` | 设置值 |
| | `set(key, value, time)` | 设置值并指定过期时间 |
| | `incr` | 递增 |
| | `decr` | 递减 |
| **Hash 操作** | `hGet` | 获取 Hash 中的值 |
| | `hGetAll` | 获取 Hash 所有键值 |
| | `hSet` | 设置 Hash 值 |
| | `hSetAll` | 批量设置 Hash 值 |
| | `hDel` | 删除 Hash 字段 |
| | `hHasKey` | 判断 Hash 字段是否存在 |
| | `hIncr` | Hash 字段递增 |
| | `hDecr` | Hash 字段递减 |
| **Set 操作** | `sGet` | 获取 Set 所有值 |
| | `sHasKey` | 判断值是否在 Set 中 |
| | `sSet` | 添加值到 Set |
| | `sGetSetSize` | 获取 Set 大小 |
| | `sRemove` | 从 Set 移除值 |
| **List 操作** | `lGet` | 获取 List 范围内的值 |
| | `lGetListSize` | 获取 List 大小 |
| | `lGetIndex` | 获取 List 指定索引的值 |
| | `lSet` | 添加值到 List |
| | `lUpdateIndex` | 更新 List 指定索引的值 |
| | `lRemove` | 从 List 移除值 |

## 4. 使用示例

### 4.1 String 操作

```java
@Service
public class UserService {

    @Autowired
    private RedisUtil redisUtil;

    // 缓存用户信息
    public void cacheUser(UserDTO user) {
        String key = "user:" + user.getId();
        redisUtil.set(key, user, 3600); // 缓存 1 小时
    }

    // 获取用户信息
    public UserDTO getUser(Long userId) {
        String key = "user:" + userId;
        Object obj = redisUtil.get(key);
        if (obj != null) {
            return (UserDTO) obj;
        }
        // 从数据库查询...
        return null;
    }

    // 计数器
    public long incrementViewCount(Long articleId) {
        String key = "article:view:" + articleId;
        return redisUtil.incr(key, 1);
    }
}
```

### 4.2 Hash 操作

```java
@Service
public class SessionService {

    @Autowired
    private RedisUtil redisUtil;

    // 保存会话信息
    public void saveSession(String sessionId, Map<String, Object> sessionData) {
        String key = "session:" + sessionId;
        redisUtil.hSetAll(key, sessionData, 1800); // 30 分钟过期
    }

    // 获取会话字段
    public Object getSessionField(String sessionId, String field) {
        String key = "session:" + sessionId;
        return redisUtil.hGet(key, field);
    }

    // 更新会话字段
    public void updateSessionField(String sessionId, String field, Object value) {
        String key = "session:" + sessionId;
        redisUtil.hSet(key, field, value);
    }

    // 删除会话
    public void deleteSession(String sessionId) {
        String key = "session:" + sessionId;
        redisUtil.del(key);
    }
}
```

### 4.3 Set 操作

```java
@Service
public class TagService {

    @Autowired
    private RedisUtil redisUtil;

    // 添加文章标签
    public void addArticleTags(Long articleId, String... tags) {
        String key = "article:tags:" + articleId;
        redisUtil.sSet(key, (Object[]) tags);
    }

    // 获取文章所有标签
    public Set<Object> getArticleTags(Long articleId) {
        String key = "article:tags:" + articleId;
        return redisUtil.sGet(key);
    }

    // 判断文章是否有某个标签
    public boolean hasTag(Long articleId, String tag) {
        String key = "article:tags:" + articleId;
        return redisUtil.sHasKey(key, tag);
    }

    // 移除标签
    public void removeTag(Long articleId, String tag) {
        String key = "article:tags:" + articleId;
        redisUtil.sRemove(key, tag);
    }
}
```

### 4.4 List 操作

```java
@Service
public class MessageService {

    @Autowired
    private RedisUtil redisUtil;

    // 添加消息到队列
    public void pushMessage(Long userId, MessageDTO message) {
        String key = "user:messages:" + userId;
        redisUtil.lSet(key, message);
    }

    // 获取最新的 10 条消息
    public List<Object> getRecentMessages(Long userId) {
        String key = "user:messages:" + userId;
        return redisUtil.lGet(key, 0, 9);
    }

    // 获取消息总数
    public long getMessageCount(Long userId) {
        String key = "user:messages:" + userId;
        return redisUtil.lGetListSize(key);
    }

    // 删除指定消息
    public void removeMessage(Long userId, MessageDTO message) {
        String key = "user:messages:" + userId;
        redisUtil.lRemove(key, 1, message);
    }
}
```

## 5. 序列化示例

### 5.1 对象序列化

**Java 对象**:

```java
@Data
public class UserDTO extends BaseDTO {
    private String username;
    private String email;
    private Integer age;
}

UserDTO user = new UserDTO();
user.setId(1234567890123456789L);
user.setUsername("张三");
user.setEmail("zhangsan@example.com");
user.setAge(25);

redisUtil.set("user:1234567890123456789", user);
```

**Redis 存储格式** (Fastjson2 序列化):

```json
{
  "@type": "org.joker.comfypilot.user.application.dto.UserDTO",
  "id": "1234567890123456789",
  "username": "张三",
  "email": "zhangsan@example.com",
  "age": 25,
  "createdAt": "2026-01-14 20:30:45",
  "updatedAt": "2026-01-14 20:30:45"
}
```

**注意**:
- `@type` 字段由 `WriteClassName` 特性自动添加
- `id` 字段被序列化为 String (由 JacksonConfig 配置)
- 反序列化时会自动根据 `@type` 恢复为 `UserDTO` 对象

### 5.2 Hash 序列化

```java
Map<String, Object> sessionData = new HashMap<>();
sessionData.put("userId", 1234567890123456789L);
sessionData.put("username", "张三");
sessionData.put("loginTime", LocalDateTime.now());

redisUtil.hSetAll("session:abc123", sessionData);
```

**Redis 存储**:

```
HGETALL session:abc123

1) "userId"
2) "\"1234567890123456789\""
3) "username"
4) "\"张三\""
5) "loginTime"
6) "\"2026-01-14 20:30:45\""
```

## 6. 注意事项

### 6.1 类型信息

✅ **推荐做法**:

```java
// 存储时指定具体类型
UserDTO user = new UserDTO();
redisUtil.set("user:1", user);

// 取出时强制转换
UserDTO cachedUser = (UserDTO) redisUtil.get("user:1");
```

❌ **不推荐**:

```java
// 使用 Object 类型存储,反序列化时可能丢失类型信息
Object obj = new UserDTO();
redisUtil.set("user:1", obj);
```

### 6.2 Long 类型处理

由于 [JacksonConfig](src/main/java/org/joker/comfypilot/common/config/JacksonConfig.java) 已配置 Long 序列化为 String:

```java
// Long 类型会自动序列化为 String
Long userId = 1234567890123456789L;
redisUtil.set("userId", userId);

// Redis 中存储为: "1234567890123456789"
// 取出时自动转换回 Long
Long cachedUserId = (Long) redisUtil.get("userId");
```

### 6.3 过期时间

```java
// 设置 1 小时过期
redisUtil.set("key", value, 3600);

// 设置 1 天过期
redisUtil.set("key", value, 86400);

// 永不过期
redisUtil.set("key", value);
```

### 6.4 批量操作

```java
// 批量删除
redisUtil.del("key1", "key2", "key3");

// 批量添加到 Set
redisUtil.sSet("tags", "Java", "Spring", "Redis");

// 批量设置 Hash
Map<String, Object> map = new HashMap<>();
map.put("field1", "value1");
map.put("field2", "value2");
redisUtil.hSetAll("hash:key", map);
```

## 7. 性能优化建议

### 7.1 合理设置过期时间

```java
// 热点数据:短过期时间
redisUtil.set("hot:data", value, 300); // 5 分钟

// 普通数据:中等过期时间
redisUtil.set("normal:data", value, 3600); // 1 小时

// 冷数据:长过期时间
redisUtil.set("cold:data", value, 86400); // 1 天
```

### 7.2 使用 Hash 减少 Key 数量

❌ **不推荐** (大量 Key):

```java
redisUtil.set("user:1:name", "张三");
redisUtil.set("user:1:email", "zhangsan@example.com");
redisUtil.set("user:1:age", 25);
```

✅ **推荐** (使用 Hash):

```java
Map<String, Object> userData = new HashMap<>();
userData.put("name", "张三");
userData.put("email", "zhangsan@example.com");
userData.put("age", 25);
redisUtil.hSetAll("user:1", userData);
```

### 7.3 避免大 Value

```java
// ❌ 不推荐:存储大对象
List<UserDTO> allUsers = userService.getAllUsers(); // 10000 条记录
redisUtil.set("all:users", allUsers);

// ✅ 推荐:分页存储
List<UserDTO> page1 = userService.getUsers(1, 100);
redisUtil.set("users:page:1", page1);
```

## 8. 常见问题

### Q1: 为什么使用 Fastjson2 而不是 Jackson?

**A**:
- **性能**: Fastjson2 序列化/反序列化性能更高
- **兼容性**: 更好地支持复杂对象和泛型
- **功能**: 提供更多特性(如 `FieldBased` 访问私有字段)

### Q2: `FieldBased` 特性有什么用?

**A**: 允许 Fastjson2 直接访问私有字段进行反序列化,无需 getter/setter:

```java
@Data
public class UserDTO {
    private Long id; // 即使没有 public getter,也能反序列化
    private String name;
}
```

### Q3: 如何处理反序列化失败?

**A**: 使用 try-catch 捕获异常:

```java
try {
    UserDTO user = (UserDTO) redisUtil.get("user:1");
} catch (ClassCastException e) {
    log.error("反序列化失败", e);
    // 从数据库重新加载
}
```

### Q4: 如何清空所有缓存?

**A**: 不建议在生产环境使用,仅用于开发/测试:

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 清空当前数据库
redisTemplate.getConnectionFactory().getConnection().flushDb();

// 清空所有数据库
redisTemplate.getConnectionFactory().getConnection().flushAll();
```

## 9. 总结

### 9.1 配置效果

✅ **String Value 使用 Fastjson2 序列化**
✅ **Hash Value 使用 Fastjson2 序列化**
✅ **支持复杂对象序列化**
✅ **自动处理 Long 类型精度问题**
✅ **提供完整的工具类方法**

### 9.2 开发规范

1. **Key 命名**: 使用冒号分隔,如 `user:1234`, `session:abc123`
2. **过期时间**: 根据数据热度合理设置
3. **类型转换**: 取出数据时进行类型转换
4. **异常处理**: 捕获序列化/反序列化异常
5. **批量操作**: 优先使用 Hash 减少 Key 数量

---

**文档版本**: v1.0
**创建日期**: 2026-01-14
**维护者**: 开发团队
