# Tool 模块 - API 设计

## 1. 模块说明

Tool 模块是**内部服务模块**，不对外暴露 REST API。

Tool 模块通过 Java 接口为其他模块（主要是 Agent 模块）提供服务。

---

## 2. 核心服务接口

### 2.1 ToolExecutor 接口

**接口路径**：`org.joker.comfypilot.tool.application.service.ToolExecutor`

**职责**：工具执行器，提供统一的工具执行入口

---

#### 2.1.1 按类型执行工具

**方法签名**：
```java
ToolExecutionResult execute(ToolType type, Map<String, Object> parameters)
```

**参数**：
- `type` (ToolType) - 工具类型枚举
- `parameters` (Map<String, Object>) - 工具参数

**返回**：
- `ToolExecutionResult` - 工具执行结果

**使用示例**：
```java
@Autowired
private ToolExecutor toolExecutor;

// 调用 LLM 工具
Map<String, Object> params = new HashMap<>();
params.put("prompt", "你好，请介绍一下自己");
params.put("temperature", 0.7);
params.put("max_tokens", 2000);

ToolExecutionResult result = toolExecutor.execute(ToolType.LLM, params);

if (result.isSuccess()) {
    String text = (String) result.getData().get("text");
    System.out.println("生成的文本: " + text);
} else {
    System.err.println("执行失败: " + result.getErrorMessage());
}
```

---

#### 2.1.2 按名称执行工具

**方法签名**：
```java
ToolExecutionResult execute(String toolName, Map<String, Object> parameters)
```

**参数**：
- `toolName` (String) - 工具名称
- `parameters` (Map<String, Object>) - 工具参数

**返回**：
- `ToolExecutionResult` - 工具执行结果

**使用示例**：
```java
// 调用 Embedding 工具
Map<String, Object> params = new HashMap<>();
params.put("text", "需要向量化的文本");

ToolExecutionResult result = toolExecutor.execute("embedding", params);

if (result.isSuccess()) {
    List<Float> vector = (List<Float>) result.getData().get("vector");
    Integer dimension = (Integer) result.getData().get("dimension");
    System.out.println("向量维度: " + dimension);
}
```

---

### 2.2 ToolRegistry 接口

**接口路径**：`org.joker.comfypilot.tool.domain.service.ToolRegistry`

**职责**：工具注册中心，管理所有工具实例

---

#### 2.2.1 获取工具（按类型）

**方法签名**：
```java
Tool getTool(ToolType type)
```

**参数**：
- `type` (ToolType) - 工具类型

**返回**：
- `Tool` - 工具实例，如果不存在返回 null

---

#### 2.2.2 获取工具（按名称）

**方法签名**：
```java
Tool getTool(String name)
```

**参数**：
- `name` (String) - 工具名称

**返回**：
- `Tool` - 工具实例，如果不存在返回 null

---

#### 2.2.3 获取所有工具

**方法签名**：
```java
List<Tool> getAllTools()
```

**返回**：
- `List<Tool>` - 所有工具列表

**使用示例**：
```java
@Autowired
private ToolRegistry toolRegistry;

// 获取所有工具
List<Tool> tools = toolRegistry.getAllTools();

for (Tool tool : tools) {
    System.out.println("工具: " + tool.getName());
    System.out.println("描述: " + tool.getDescription());
    System.out.println("参数 Schema: " + tool.getParameterSchema());
}
```

---

#### 2.2.4 检查工具是否存在

**方法签名**：
```java
boolean exists(ToolType type)
boolean exists(String name)
```

**参数**：
- `type` (ToolType) - 工具类型
- `name` (String) - 工具名称

**返回**：
- `boolean` - true-存在，false-不存在

---

## 3. Tool 接口

**接口路径**：`org.joker.comfypilot.tool.domain.service.Tool`

**职责**：工具接口，所有工具都需要实现此接口

---

### 3.1 获取工具信息

**方法签名**：
```java
ToolType getType()
String getName()
String getDescription()
Map<String, Object> getParameterSchema()
```

---

### 3.2 执行工具

**方法签名**：
```java
ToolExecutionResult execute(Map<String, Object> parameters)
```

**参数**：
- `parameters` (Map<String, Object>) - 工具参数

**返回**：
- `ToolExecutionResult` - 执行结果

---

## 4. 数据结构

### 4.1 ToolExecutionResult

**字段**：
```java
public class ToolExecutionResult {
    private boolean success;              // 是否成功
    private Map<String, Object> data;     // 结果数据
    private String errorMessage;          // 错误信息
    private ToolExecutionMetadata metadata; // 执行元数据
}
```

---

### 4.2 ToolExecutionMetadata

**字段**：
```java
public class ToolExecutionMetadata {
    private String toolType;           // 工具类型
    private String toolName;           // 工具名称
    private Long executionTimeMs;      // 执行时间（毫秒）
    private String modelUsed;          // 使用的模型
    private Integer tokenUsed;         // Token 使用量
    private Integer inputTokens;       // 输入 Token
    private Integer outputTokens;      // 输出 Token
}
```

---

## 5. 使用场景

### 5.1 Agent 调用 LLM 工具

```java
@Service
public class AgentService {

    @Autowired
    private ToolExecutor toolExecutor;

    public String generateText(String prompt) {
        // 构建参数
        Map<String, Object> params = new HashMap<>();
        params.put("prompt", prompt);
        params.put("temperature", 0.7);

        // 执行工具
        ToolExecutionResult result = toolExecutor.execute(ToolType.LLM, params);

        // 处理结果
        if (result.isSuccess()) {
            return (String) result.getData().get("text");
        } else {
            throw new BusinessException("文本生成失败: " + result.getErrorMessage());
        }
    }
}
```

---

### 5.2 Agent 调用 Embedding 工具

```java
@Service
public class AgentService {

    @Autowired
    private ToolExecutor toolExecutor;

    public List<Float> generateEmbedding(String text) {
        // 构建参数
        Map<String, Object> params = new HashMap<>();
        params.put("text", text);

        // 执行工具
        ToolExecutionResult result = toolExecutor.execute(ToolType.EMBEDDING, params);

        // 处理结果
        if (result.isSuccess()) {
            return (List<Float>) result.getData().get("vector");
        } else {
            throw new BusinessException("向量生成失败: " + result.getErrorMessage());
        }
    }
}
```

---

### 5.3 获取所有可用工具

```java
@Service
public class AgentService {

    @Autowired
    private ToolRegistry toolRegistry;

    public List<ToolInfo> getAvailableTools() {
        List<Tool> tools = toolRegistry.getAllTools();

        return tools.stream()
            .map(tool -> ToolInfo.builder()
                .type(tool.getType().getCode())
                .name(tool.getName())
                .description(tool.getDescription())
                .parameterSchema(tool.getParameterSchema())
                .build())
            .collect(Collectors.toList());
    }
}
```

---

## 6. 异常处理

### 6.1 工具不存在

当工具不存在时，返回失败结果：
```java
ToolExecutionResult.failure("工具不存在: " + type)
```

---

### 6.2 参数验证失败

当参数验证失败时，返回失败结果：
```java
ToolExecutionResult.failure("缺少必需参数: prompt")
```

---

### 6.3 执行异常

当执行过程中发生异常时，返回失败结果：
```java
ToolExecutionResult.failure("工具执行失败: " + e.getMessage())
```

---

## 7. 性能考虑

### 7.1 工具注册

- 工具在 Spring 启动时自动注册
- 使用 ConcurrentHashMap 存储，支持并发访问
- 注册过程只执行一次

---

### 7.2 工具执行

- 记录执行时间
- 异步执行（如果需要）
- 超时控制（由底层 Model 模块控制）

---

## 8. 扩展性

### 8.1 添加新工具

1. 实现 Tool 接口
2. 添加 @Component 注解
3. 自动注册到 ToolRegistry

---

### 8.2 自定义工具参数

通过 getParameterSchema() 方法定义 JSON Schema

---

### 8.3 自定义工具返回

通过 ToolExecutionResult 的 data 字段返回任意结构的数据
