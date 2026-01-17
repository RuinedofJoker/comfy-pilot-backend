# Step10: Tool 模块实现

> 本文档记录 Step10 实现 Tool 模块，为 Agent 提供统一的工具调用接口

## 📋 目录

- [一、Step9 完成情况回顾](#一step9-完成情况回顾)
- [二、Step10 目标](#二step10-目标)
- [三、架构设计](#三架构设计)
- [四、实现内容](#四实现内容)
- [五、实现文件清单](#五实现文件清单)
- [六、当前进度](#六当前进度)

---

## 一、Step9 完成情况回顾

### 1.1 Step9 已完成内容

Step9 完成了 Model 模块的能力抽象层：

1. **模型能力抽象层** ✅
   - ModelCapability 枚举（TEXT_GENERATION、EMBEDDING 等）
   - ModelCapabilityService 统一能力调用接口
   - 统一的请求/响应格式

2. **能力路由与调度层** ✅
   - CapabilityRouter 根据能力类型选择模型
   - 支持约束条件（优先本地、成本限制等）

3. **模型执行器层** ✅
   - ModelExecutor 统一执行器接口
   - RemoteApiExecutor 远程 API 执行器抽象类
   - OpenAiExecutor 使用 langchain4j 实现
   - DefaultOpenAiExecutor 默认实现
   - CustomOpenAiExecutor 自定义实现示例

### 1.2 Step9 的成果

- ✅ 提供了统一的模型能力调用接口
- ✅ 实现了能力路由和调度机制
- ✅ 完成了 OpenAI 模型的实际调用
- ✅ 为 Tool 层提供了清晰的集成点

---

## 二、Step10 目标

### 2.1 核心目标

**实现 Tool 模块，为 Agent 提供统一的工具调用接口，并集成 Model 模块的能力**

### 2.2 设计原则

1. **工具即能力**：每个 Tool 封装一种特定能力
2. **统一接口**：所有 Tool 实现统一的接口
3. **参数标准化**：使用 JSON Schema 定义工具参数
4. **结果标准化**：统一的执行结果格式
5. **可扩展性**：易于添加新的工具类型

### 2.3 功能范围

本 Step 实现以下内容：

1. **Tool 基础框架**
   - 定义 Tool 接口
   - 定义 ToolType 枚举
   - 定义统一的参数和结果格式

2. **基础 Tool 实现**
   - LlmTool - 大语言模型工具
   - EmbeddingTool - 向量生成工具

3. **Tool 管理服务**
   - ToolRegistry - 工具注册中心
   - ToolExecutor - 工具执行器

4. **集成 Model 模块**
   - Tool 调用 ModelCapabilityService
   - 测试 Agent → Tool → Model 完整链路

---

## 三、架构设计

### 3.1 整体架构层次

```
┌─────────────────────────────────────────────────────────┐
│                    Agent Layer (决策层)                   │
│              Agent 决策使用哪个 Tool                       │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Tool Layer (工具层)                      │
│                  ← 当前 Step10 实现                       │
│  ┌─────────────────────────────────────────────────┐   │
│  │  1. Tool 接口层                                  │   │
│  │     - Tool 接口定义                              │   │
│  │     - ToolType 枚举                             │   │
│  │     - 参数和结果格式                             │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  2. Tool 实现层                                  │   │
│  │     - LlmTool (文本生成)                        │   │
│  │     - EmbeddingTool (向量生成)                  │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  3. Tool 管理层                                  │   │
│  │     - ToolRegistry (工具注册)                   │   │
│  │     - ToolExecutor (工具执行)                   │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│          Model Capability Layer (模型能力层)              │
│              ModelCapabilityService                      │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Tool 调用流程

```
Agent
  ↓ 1. 选择 Tool
ToolExecutor
  ↓ 2. 查找 Tool 实例
ToolRegistry
  ↓ 3. 执行 Tool
Tool (LlmTool/EmbeddingTool)
  ↓ 4. 调用模型能力
ModelCapabilityService
  ↓ 5. 路由到具体模型
CapabilityRouter
  ↓ 6. 执行模型推理
ModelExecutor (OpenAiExecutor)
  ↓ 7. 返回结果
Tool → Agent
```

### 3.3 核心组件关系

```
ToolExecutor (工具执行器)
    ↓
ToolRegistry (工具注册中心)
    ↓
Tool 接口
    ├── LlmTool (调用 ModelCapabilityService)
    └── EmbeddingTool (调用 ModelCapabilityService)
```

---

## 四、实现内容

### 4.1 Tool 基础框架

#### 4.1.1 ToolType 枚举

**文件路径**: `src/main/java/org/joker/comfypilot/tool/domain/enums/ToolType.java`

**工具类型定义**:
```java
public enum ToolType {
    LLM("llm", "大语言模型工具"),
    EMBEDDING("embedding", "向量生成工具"),
    CLASSIFICATION("classification", "文本分类工具"),
    SENTIMENT_ANALYSIS("sentiment_analysis", "情感分析工具"),
    ENTITY_RECOGNITION("entity_recognition", "实体识别工具"),
    RERANK("rerank", "重排序工具");

    private final String code;
    private final String description;
}
```

#### 4.1.2 Tool 接口

**文件路径**: `src/main/java/org/joker/comfypilot/tool/domain/service/Tool.java`

**核心方法**:
```java
public interface Tool {
    /**
     * 获取工具类型
     */
    ToolType getType();

    /**
     * 获取工具名称
     */
    String getName();

    /**
     * 获取工具描述
     */
    String getDescription();

    /**
     * 获取参数 Schema (JSON Schema 格式)
     */
    Map<String, Object> getParameterSchema();

    /**
     * 执行工具
     */
    ToolExecutionResult execute(Map<String, Object> parameters);
}
```

#### 4.1.3 工具执行结果

**文件路径**: `src/main/java/org/joker/comfypilot/tool/domain/valueobject/ToolExecutionResult.java`

**结果格式**:
```java
@Data
@Builder
public class ToolExecutionResult {
    private boolean success;
    private Map<String, Object> data;
    private String errorMessage;
    private ToolExecutionMetadata metadata;
}
```

**元数据**:
```java
@Data
@Builder
public class ToolExecutionMetadata {
    private String toolType;
    private String toolName;
    private Long executionTimeMs;
    private String modelUsed;
    private Integer tokenUsed;
}
```

---

### 4.2 基础 Tool 实现

#### 4.2.1 LlmTool - 大语言模型工具

**文件路径**: `src/main/java/org/joker/comfypilot/tool/infrastructure/tool/LlmTool.java`

**功能说明**:
- 封装文本生成能力
- 调用 ModelCapabilityService 的 TEXT_GENERATION 能力
- 支持参数：prompt, temperature, max_tokens 等

**实现示例**:
```java
@Slf4j
@Component
public class LlmTool implements Tool {

    private final ModelCapabilityService modelCapabilityService;

    @Override
    public ToolType getType() {
        return ToolType.LLM;
    }

    @Override
    public String getName() {
        return "llm";
    }

    @Override
    public String getDescription() {
        return "大语言模型工具，用于文本生成和对话";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        // 返回 JSON Schema 格式的参数定义
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> parameters) {
        // 1. 构建 ModelCapabilityRequest
        ModelCapabilityRequest request = ModelCapabilityRequest.builder()
                .capability(ModelCapability.TEXT_GENERATION)
                .parameters(parameters)
                .build();

        // 2. 调用 ModelCapabilityService
        ModelCapabilityResponse response = modelCapabilityService.invoke(request);

        // 3. 构建 ToolExecutionResult
        return ToolExecutionResult.builder()
                .success(true)
                .data(response.getResult())
                .metadata(buildMetadata(response))
                .build();
    }
}
```

#### 4.2.2 EmbeddingTool - 向量生成工具

**文件路径**: `src/main/java/org/joker/comfypilot/tool/infrastructure/tool/EmbeddingTool.java`

**功能说明**:
- 封装向量生成能力
- 调用 ModelCapabilityService 的 EMBEDDING 能力
- 支持参数：text

**实现示例**:
```java
@Slf4j
@Component
public class EmbeddingTool implements Tool {

    private final ModelCapabilityService modelCapabilityService;

    @Override
    public ToolType getType() {
        return ToolType.EMBEDDING;
    }

    @Override
    public String getName() {
        return "embedding";
    }

    @Override
    public String getDescription() {
        return "向量生成工具，用于文本向量化";
    }

    @Override
    public ToolExecutionResult execute(Map<String, Object> parameters) {
        // 调用 EMBEDDING 能力
    }
}
```

---

### 4.3 Tool 管理服务

#### 4.3.1 ToolRegistry - 工具注册中心

**文件路径**: `src/main/java/org/joker/comfypilot/tool/domain/service/ToolRegistry.java`

**核心方法**:
```java
public interface ToolRegistry {
    /**
     * 注册工具
     */
    void register(Tool tool);

    /**
     * 根据类型获取工具
     */
    Tool getTool(ToolType type);

    /**
     * 根据名称获取工具
     */
    Tool getTool(String name);

    /**
     * 获取所有工具
     */
    List<Tool> getAllTools();
}
```

#### 4.3.2 ToolRegistryImpl - 实现

**文件路径**: `src/main/java/org/joker/comfypilot/tool/infrastructure/service/ToolRegistryImpl.java`

**实现说明**:
- 使用 Map 存储工具实例
- 支持按类型和名称查找
- Spring 启动时自动注册所有 Tool Bean

**实现示例**:
```java
@Slf4j
@Component
public class ToolRegistryImpl implements ToolRegistry, ApplicationContextAware {

    private final Map<ToolType, Tool> toolsByType = new ConcurrentHashMap<>();
    private final Map<String, Tool> toolsByName = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        // 自动注册所有 Tool Bean
        Map<String, Tool> tools = applicationContext.getBeansOfType(Tool.class);
        tools.values().forEach(this::register);
    }

    @Override
    public void register(Tool tool) {
        toolsByType.put(tool.getType(), tool);
        toolsByName.put(tool.getName(), tool);
        log.info("注册工具: type={}, name={}", tool.getType(), tool.getName());
    }
}
```

#### 4.3.3 ToolExecutor - 工具执行器

**文件路径**: `src/main/java/org/joker/comfypilot/tool/application/service/ToolExecutor.java`

**核心方法**:
```java
public interface ToolExecutor {
    /**
     * 执行工具（按类型）
     */
    ToolExecutionResult execute(ToolType type, Map<String, Object> parameters);

    /**
     * 执行工具（按名称）
     */
    ToolExecutionResult execute(String toolName, Map<String, Object> parameters);
}
```

**实现示例**:
```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ToolExecutorImpl implements ToolExecutor {

    private final ToolRegistry toolRegistry;

    @Override
    public ToolExecutionResult execute(ToolType type, Map<String, Object> parameters) {
        Tool tool = toolRegistry.getTool(type);
        if (tool == null) {
            throw new BusinessException("工具不存在: " + type);
        }

        log.info("执行工具: type={}, parameters={}", type, parameters);
        return tool.execute(parameters);
    }
}
```

---

## 五、实现文件清单

### 5.1 领域层（domain/）

**枚举类**:
- ⏳ ToolType.java - 工具类型枚举

**值对象**:
- ⏳ ToolExecutionResult.java - 工具执行结果
- ⏳ ToolExecutionMetadata.java - 执行元数据

**领域服务接口**:
- ⏳ Tool.java - 工具接口
- ⏳ ToolRegistry.java - 工具注册中心接口

### 5.2 应用层（application/）

**服务接口**:
- ⏳ ToolExecutor.java - 工具执行器接口

**服务实现**:
- ⏳ ToolExecutorImpl.java - 工具执行器实现

### 5.3 基础设施层（infrastructure/）

**领域服务实现**:
- ⏳ ToolRegistryImpl.java - 工具注册中心实现

**工具实现**:
- ⏳ LlmTool.java - 大语言模型工具
- ⏳ EmbeddingTool.java - 向量生成工具

**预计总文件数**: 9 个

---

## 六、当前进度

### 6.1 完成度统计

**总体进度**: 100% ✅

**分层完成度**:
- [x] 领域层 - 100% ✅
- [x] 应用层 - 100% ✅
- [x] 基础设施层 - 100% ✅

### 6.2 已完成文件清单

**领域层（5个文件）**:
1. ✅ ToolType.java - 工具类型枚举
2. ✅ ToolExecutionResult.java - 工具执行结果
3. ✅ ToolExecutionMetadata.java - 执行元数据
4. ✅ Tool.java - 工具接口
5. ✅ ToolRegistry.java - 工具注册中心接口

**应用层（2个文件）**:
6. ✅ ToolExecutor.java - 工具执行器接口
7. ✅ ToolExecutorImpl.java - 工具执行器实现

**基础设施层（3个文件）**:
8. ✅ ToolRegistryImpl.java - 工具注册中心实现
9. ✅ LlmTool.java - 大语言模型工具
10. ✅ EmbeddingTool.java - 向量生成工具

**实际完成文件数**: 10 个

---

**Step10 状态**: ✅ 已完成

**创建时间**: 2026-01-17

**完成时间**: 2026-01-17

---
