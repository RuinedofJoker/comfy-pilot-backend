# Step9: Model 模块能力抽象层实现

> 本文档记录 Step9 实现 Model 模块的能力抽象层，为 Agent 提供统一的模型能力接口

## 📋 目录

- [一、Step8 完成情况回顾](#一step8-完成情况回顾)
- [二、Step9 目标](#二step9-目标)
- [三、架构设计](#三架构设计)
- [四、实现内容](#四实现内容)
- [五、实现文件清单](#五实现文件清单)
- [六、当前进度](#六当前进度)

---

## 一、Step8 完成情况回顾

### 1.1 Step8 已完成内容

Step8 完成了 Model 模块的基础管理功能：

1. **模型提供商管理** ✅
   - ModelProvider 实体和 CRUD 接口
   - 支持 OpenAI、Anthropic、阿里云等提供商

2. **AI 模型管理** ✅
   - AiModel 实体和 CRUD 接口
   - 支持远程 API 和本地接入两种方式
   - 支持多种模型类型（LLM、Embedding 等）
   - 支持模型来源区分（remote_api / code_defined）

3. **API 密钥管理** ✅
   - ModelApiKey 实体和 CRUD 接口
   - AES 加密存储
   - 查询时脱敏显示

### 1.2 Step8 的局限性

当前实现只是"模型配置管理"，缺少：
- ❌ 统一的能力抽象接口
- ❌ 能力路由与调度机制
- ❌ 模型执行器（实际调用模型）
- ❌ 与 Tool 层的集成点

---

## 二、Step9 目标

### 2.1 核心目标

**实现面向 Agent 的统一模型能力接入与调度体系（Model Capability Layer）**

### 2.2 设计原则

1. **Agent 只负责决策**：不直接接触模型
2. **Tool 是唯一接口**：Agent 通过 Tool 调用能力
3. **能力平级设计**：LLM、Embedding、分类等是平级的能力
4. **实现透明**：模型来源、部署方式对 Agent 完全透明

### 2.3 功能范围

本 Step 实现以下内容：

1. **模型能力抽象层**
   - 定义 ModelCapability 枚举（能力类型）
   - 定义统一的能力调用接口
   - 定义统一的请求/响应格式

2. **能力路由与调度层**
   - 根据能力类型选择合适的模型
   - 支持约束条件（优先本地、成本限制等）
   - 支持多模型负载均衡

3. **模型执行器层（基础实现）**
   - 定义统一的执行器接口
   - 实现远程 API 执行器（OpenAI/Claude）
   - 预留本地模型执行器接口

4. **模型配置增强**
   - 在 model_config 中配置支持的能力
   - 配置能力参数（如 max_tokens、temperature 等）

---

## 三、架构设计

### 3.1 整体架构层次

```
┌─────────────────────────────────────────────────────────┐
│                    Agent Layer (决策层)                   │
│                  只通过 Tool 调用能力                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│                  Tool Layer (能力抽象层)                   │
│         EmbeddingTool / ClassificationTool / ...         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│          Model Capability Layer (模型能力层)              │
│                  ← 当前 Step9 实现                        │
│  ┌─────────────────────────────────────────────────┐   │
│  │  1. 能力抽象层 (ModelCapabilityService)          │   │
│  │     - 统一的能力调用接口                         │   │
│  │     - 统一的请求/响应格式                        │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  2. 能力路由层 (CapabilityRouter)                │   │
│  │     - 根据能力类型选择模型                       │   │
│  │     - 支持约束条件和负载均衡                     │   │
│  └─────────────────────────────────────────────────┘   │
│  ┌─────────────────────────────────────────────────┐   │
│  │  3. 模型执行器层 (ModelExecutor)                 │   │
│  │     - RemoteApiExecutor (远程 API)              │   │
│  │     - LocalModelExecutor (本地模型，预留)        │   │
│  └─────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────┐
│            Model Provider (模型提供者)                    │
│    OpenAI / Claude / 通义千问 / Ollama / ...            │
└─────────────────────────────────────────────────────────┘
```

### 3.2 核心组件关系

```
ModelCapabilityService (统一入口)
    ↓
CapabilityRouter (路由选择)
    ↓
ModelExecutor (执行推理)
    ↓
实际模型 API 调用
```

---

## 四、实现内容

### 4.1 模型能力抽象层

#### 4.1.1 ModelCapability 枚举

**文件路径**: `src/main/java/org/joker/comfypilot/model/domain/enums/ModelCapability.java`

**能力类型定义**:
```java
public enum ModelCapability {
    TEXT_GENERATION("text_generation", "文本生成/对话"),
    EMBEDDING("embedding", "向量生成"),
    CLASSIFICATION("classification", "文本分类"),
    SENTIMENT_ANALYSIS("sentiment_analysis", "情感分析"),
    ENTITY_RECOGNITION("entity_recognition", "实体识别"),
    RERANK("rerank", "重排序");

    private final String code;
    private final String description;
}
```

#### 4.1.2 统一请求/响应格式

**ModelCapabilityRequest**:
```java
@Data
@Builder
public class ModelCapabilityRequest {
    private ModelCapability capability;
    private Map<String, Object> parameters;
    private CapabilityConstraints constraints;
}
```

**ModelCapabilityResponse**:
```java
@Data
@Builder
public class ModelCapabilityResponse {
    private ModelCapability capability;
    private Map<String, Object> result;
    private ModelExecutionMetadata metadata;
}
```

#### 4.1.3 ModelCapabilityService 接口

**文件路径**: `src/main/java/org/joker/comfypilot/model/application/service/ModelCapabilityService.java`

**核心方法**:
```java
public interface ModelCapabilityService {
    /**
     * 调用模型能力
     */
    ModelCapabilityResponse invoke(ModelCapabilityRequest request);

    /**
     * 获取支持某能力的所有模型
     */
    List<AiModel> getModelsForCapability(ModelCapability capability);
}
```

---

### 4.2 能力路由与调度层

#### 4.2.1 CapabilityRouter 接口

**文件路径**: `src/main/java/org/joker/comfypilot/model/domain/service/CapabilityRouter.java`

**核心方法**:
```java
public interface CapabilityRouter {
    /**
     * 根据能力类型选择最佳模型
     */
    AiModel selectModel(ModelCapability capability, CapabilityConstraints constraints);

    /**
     * 获取支持某能力的所有模型
     */
    List<AiModel> getModelsForCapability(ModelCapability capability);
}
```

#### 4.2.2 CapabilityRouterImpl 实现

**路由策略**:
1. 根据 model_config 中的 capabilities 字段过滤模型
2. 根据约束条件（优先本地、成本限制等）排序
3. 选择优先级最高的可用模型

---

### 4.3 模型执行器层

#### 4.3.1 ModelExecutor 接口

**文件路径**: `src/main/java/org/joker/comfypilot/model/domain/service/ModelExecutor.java`

**核心方法**:
```java
public interface ModelExecutor {
    /**
     * 执行模型推理
     */
    ModelExecutionResult execute(ModelExecutionRequest request);

    /**
     * 支持的能力类型
     */
    Set<ModelCapability> supportedCapabilities();

    /**
     * 是否支持指定模型
     */
    boolean supports(AiModel model);
}
```

#### 4.3.2 RemoteApiExecutor 抽象类

**文件路径**: `src/main/java/org/joker/comfypilot/model/infrastructure/executor/RemoteApiExecutor.java`

**职责**:
- 提供远程 API 调用的通用逻辑
- 验证模型状态（是否启用、是否有提供商）
- 验证能力支持
- 模板方法模式，子类实现具体调用逻辑

**核心方法**:
```java
public abstract class RemoteApiExecutor implements ModelExecutor {
    @Override
    public Map<String, Object> execute(AiModel model, ModelCapability capability,
                                       Map<String, Object> parameters) {
        validateModel(model);
        if (!supportedCapabilities().contains(capability)) {
            throw new BusinessException("执行器不支持该能力: " + capability);
        }
        return doExecute(model, capability, parameters);
    }

    protected abstract Map<String, Object> doExecute(AiModel model, ModelCapability capability,
                                                     Map<String, Object> parameters);
    protected abstract boolean supportsProviderType(AiModel model);
}
```

#### 4.3.3 OpenAiExecutor 抽象类（使用 langchain4j）

**文件路径**: `src/main/java/org/joker/comfypilot/model/infrastructure/executor/OpenAiExecutor.java`

**设计理念**:
- 使用 langchain4j 封装 OpenAI API 调用
- 抽象类提供完整的核心实现
- 子类只需重写 3 个方法即可使用：
  - `getApiBaseUrl(AiModel model)` - 设置 API Base URL
  - `getApiKey(AiModel model)` - 设置 API Key
  - `getModelParameters(AiModel model, Map<String, Object> requestParameters)` - 设置模型参数

**核心实现**:
```java
@Slf4j
@RequiredArgsConstructor
public abstract class OpenAiExecutor extends RemoteApiExecutor {

    private final ModelProviderRepository providerRepository;
    private final ModelApiKeyRepository apiKeyRepository;

    @Override
    protected Map<String, Object> doExecute(AiModel model, ModelCapability capability,
                                            Map<String, Object> parameters) {
        return switch (capability) {
            case TEXT_GENERATION -> executeTextGeneration(model, parameters);
            case EMBEDDING -> executeEmbedding(model, parameters);
            default -> throw new BusinessException("不支持的能力类型: " + capability);
        };
    }

    // 文本生成实现（使用 langchain4j OpenAiChatModel）
    private Map<String, Object> executeTextGeneration(AiModel model, Map<String, Object> parameters) {
        ChatLanguageModel chatModel = buildChatModel(model, parameters);
        Response<AiMessage> response = chatModel.generate(prompt);
        // 返回结果包含 text, token 使用信息等
    }

    // 向量生成实现（使用 langchain4j OpenAiEmbeddingModel）
    private Map<String, Object> executeEmbedding(AiModel model, Map<String, Object> parameters) {
        EmbeddingModel embeddingModel = buildEmbeddingModel(model, parameters);
        Response<Embedding> response = embeddingModel.embed(text);
        // 返回结果包含 vector, dimension 等
    }

    // 子类可重写的方法（提供默认实现）
    protected String getApiBaseUrl(AiModel model) {
        // 默认从 ModelProvider 获取
    }

    protected String getApiKey(AiModel model) {
        // 默认从 ModelApiKey 获取第一个启用的密钥
    }

    protected Map<String, Object> getModelParameters(AiModel model, Map<String, Object> requestParameters) {
        // 默认直接返回请求参数
    }
}
```

**支持的能力**:
- TEXT_GENERATION - 文本生成/对话
- EMBEDDING - 向量生成

**langchain4j 集成**:
- 使用 `OpenAiChatModel` 处理文本生成
- 使用 `OpenAiEmbeddingModel` 处理向量生成
- 自动处理 token 统计
- 支持参数配置（temperature, max_tokens, top_p 等）

#### 4.3.4 DefaultOpenAiExecutor 默认实现

**文件路径**: `src/main/java/org/joker/comfypilot/model/infrastructure/executor/DefaultOpenAiExecutor.java`

**设计说明**:
- 直接继承 `OpenAiExecutor`，无需重写任何方法
- 使用父类的默认实现：
  - API Base URL 从 `ModelProvider` 获取
  - API Key 从 `ModelApiKey` 获取第一个启用的密钥
  - 模型参数直接使用请求参数

**实现代码**:
```java
@Slf4j
@Component
public class DefaultOpenAiExecutor extends OpenAiExecutor {

    public DefaultOpenAiExecutor(ModelProviderRepository providerRepository,
                                 ModelApiKeyRepository apiKeyRepository) {
        super(providerRepository, apiKeyRepository);
    }

    // 使用父类的默认实现，无需重写任何方法
}
```

**适用场景**:
- 标准的 OpenAI API 调用
- API 配置存储在数据库中
- 不需要特殊的参数处理逻辑

#### 4.3.5 CustomOpenAiExecutor 自定义实现示例

**文件路径**: `src/main/java/org/joker/comfypilot/model/infrastructure/executor/CustomOpenAiExecutor.java`

**设计说明**:
- 演示如何通过重写方法来自定义配置
- 支持从环境变量、配置文件等获取配置
- 支持自定义参数处理逻辑

**实现示例**:
```java
@Slf4j
// @Component  // 如果需要使用此实现，取消注释
public class CustomOpenAiExecutor extends OpenAiExecutor {

    @Override
    protected String getApiBaseUrl(AiModel model) {
        // 示例：使用固定的代理地址
        // return "https://api.openai-proxy.com/v1";

        // 或者根据模型动态选择
        // if (model.getModelIdentifier().startsWith("gpt-4")) {
        //     return "https://api.openai-gpt4.com/v1";
        // }

        return super.getApiBaseUrl(model);
    }

    @Override
    protected String getApiKey(AiModel model) {
        // 示例：从环境变量获取
        // String apiKey = System.getenv("OPENAI_API_KEY");
        // if (apiKey != null && !apiKey.isEmpty()) {
        //     return apiKey;
        // }

        return super.getApiKey(model);
    }

    @Override
    protected Map<String, Object> getModelParameters(AiModel model, Map<String, Object> requestParameters) {
        Map<String, Object> params = new HashMap<>(requestParameters);

        // 添加默认参数
        params.putIfAbsent("temperature", 0.7);
        params.putIfAbsent("max_tokens", 2000);

        // 参数转换
        if (params.containsKey("max_length")) {
            params.put("max_tokens", params.remove("max_length"));
        }

        // 参数校验和限制
        if (params.containsKey("temperature")) {
            double temp = ((Number) params.get("temperature")).doubleValue();
            if (temp < 0 || temp > 2) {
                log.warn("temperature 超出范围 [0, 2]，使用默认值 0.7");
                params.put("temperature", 0.7);
            }
        }

        return params;
    }
}
```

**适用场景**:
- 使用代理服务访问 OpenAI API
- API Key 存储在配置文件或密钥管理服务中
- 需要对参数进行转换或校验
- 需要添加默认参数

---

### 4.4 模型配置增强

#### 4.4.1 model_config JSON 结构

**配置示例**:
```json
{
  "capabilities": ["TEXT_GENERATION", "CLASSIFICATION"],
  "parameters": {
    "max_tokens": 4096,
    "temperature": 0.7,
    "top_p": 1.0
  },
  "priority": 10,
  "cost_per_1k_tokens": 0.002
}
```

#### 4.4.2 ModelConfig 实体类

**文件路径**: `src/main/java/org/joker/comfypilot/model/domain/valueobject/ModelConfig.java`

**字段定义**:
```java
@Data
@Builder
public class ModelConfig {
    private List<ModelCapability> capabilities;
    private Map<String, Object> parameters;
    private Integer priority;
    private Double costPer1kTokens;
}
```

---

## 五、实现文件清单

### 5.1 领域层（domain/）

**枚举类**:
- ⏳ ModelCapability.java - 模型能力类型枚举

**值对象**:
- ⏳ ModelConfig.java - 模型配置值对象
- ⏳ CapabilityConstraints.java - 能力约束条件
- ⏳ ModelExecutionMetadata.java - 执行元数据

**领域服务接口**:
- ⏳ CapabilityRouter.java - 能力路由接口
- ⏳ ModelExecutor.java - 模型执行器接口

### 5.2 应用层（application/）

**请求/响应类**:
- ⏳ ModelCapabilityRequest.java - 能力调用请求
- ⏳ ModelCapabilityResponse.java - 能力调用响应
- ⏳ ModelExecutionRequest.java - 执行请求
- ⏳ ModelExecutionResult.java - 执行结果

**服务接口**:
- ⏳ ModelCapabilityService.java - 模型能力服务接口

**服务实现**:
- ⏳ ModelCapabilityServiceImpl.java - 模型能力服务实现

### 5.3 基础设施层（infrastructure/）

**领域服务实现**:
- ⏳ CapabilityRouterImpl.java - 能力路由实现

**执行器实现**:
- ⏳ RemoteApiExecutor.java - 远程 API 执行器
- ⏳ OpenAiExecutor.java - OpenAI 专用执行器
- ⏳ ClaudeExecutor.java - Claude 专用执行器

**工具类**:
- ⏳ ModelConfigParser.java - 模型配置解析工具

**预计总文件数**: 16 个

---

## 六、当前进度

### 6.1 完成度统计

**总体进度**: 100% ✅

**分层完成度**:
- [x] 领域层 - 100% ✅
- [x] 应用层 - 100% ✅
- [x] 基础设施层 - 100% ✅

### 6.2 已完成文件清单

**领域层（6个文件）**:
1. ✅ ModelCapability.java - 模型能力类型枚举
2. ✅ ModelConfig.java - 模型配置值对象
3. ✅ CapabilityConstraints.java - 能力约束条件值对象
4. ✅ ModelExecutionMetadata.java - 执行元数据值对象
5. ✅ CapabilityRouter.java - 能力路由接口
6. ✅ ModelExecutor.java - 模型执行器接口

**应用层（4个文件）**:
7. ✅ ModelCapabilityRequest.java - 能力调用请求
8. ✅ ModelCapabilityResponse.java - 能力调用响应
9. ✅ ModelCapabilityService.java - 模型能力服务接口
10. ✅ ModelCapabilityServiceImpl.java - 模型能力服务实现

**基础设施层（5个文件）**:
11. ✅ CapabilityRouterImpl.java - 能力路由实现
12. ✅ RemoteApiExecutor.java - 远程API执行器抽象类
13. ✅ OpenAiExecutor.java - OpenAI执行器抽象类（使用 langchain4j）
14. ✅ DefaultOpenAiExecutor.java - OpenAI 默认实现
15. ✅ CustomOpenAiExecutor.java - OpenAI 自定义实现示例

**依赖管理（1个文件）**:
16. ✅ pom.xml - 添加 langchain4j 依赖

**实际完成文件数**: 16 个

---

**Step9 状态**: ✅ 已完成

**创建时间**: 2026-01-17

**完成时间**: 2026-01-17

---

## 七、OpenAiExecutor 重构总结

### 7.1 重构内容

在 Step9 的基础上，对 OpenAiExecutor 进行了重构，使其更加灵活和易于扩展：

**重构前**:
- OpenAiExecutor 是一个具体的 @Component 类
- 返回模拟数据，没有实际的 API 调用实现
- 无法灵活配置 URL、API Key 和参数

**重构后**:
- OpenAiExecutor 改为抽象类，提供完整的 langchain4j 实现
- 子类只需重写 3 个方法即可使用：
  - `getApiBaseUrl()` - 设置 API Base URL
  - `getApiKey()` - 设置 API Key
  - `getModelParameters()` - 设置模型参数
- 提供默认实现和自定义实现示例

### 7.2 技术栈集成

**langchain4j 版本**: 1.3.0

**添加的依赖**:
```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
</dependency>
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
</dependency>
```

**使用的 langchain4j 组件**:
- `OpenAiChatModel` - 文本生成/对话
- `OpenAiEmbeddingModel` - 向量生成
- `Response<AiMessage>` - 聊天响应
- `Response<Embedding>` - 向量响应

### 7.3 设计优势

**1. 模板方法模式**:
- 父类定义算法骨架（核心调用逻辑）
- 子类只需实现特定步骤（URL、Key、参数）

**2. 开闭原则（OCP）**:
- 对扩展开放：可以轻松创建新的子类实现
- 对修改封闭：核心逻辑在父类中，无需修改

**3. 依赖注入**:
- 通过构造函数注入 Repository
- 支持 Spring 依赖管理

**4. 灵活配置**:
- 默认实现：从数据库获取配置
- 自定义实现：从环境变量、配置文件等获取
- 支持参数转换和校验

### 7.4 使用示例

**场景 1：使用默认实现**
```java
// DefaultOpenAiExecutor 已注册为 @Component
// Spring 自动注入，无需额外配置
// API 配置从数据库的 ModelProvider 和 ModelApiKey 获取
```

**场景 2：使用代理服务**
```java
@Component
public class ProxyOpenAiExecutor extends OpenAiExecutor {
    @Override
    protected String getApiBaseUrl(AiModel model) {
        return "https://api.openai-proxy.com/v1";
    }
}
```

**场景 3：使用环境变量**
```java
@Component
public class EnvOpenAiExecutor extends OpenAiExecutor {
    @Override
    protected String getApiKey(AiModel model) {
        return System.getenv("OPENAI_API_KEY");
    }
}
```

**场景 4：添加默认参数**
```java
@Component
public class DefaultParamsOpenAiExecutor extends OpenAiExecutor {
    @Override
    protected Map<String, Object> getModelParameters(AiModel model, Map<String, Object> requestParameters) {
        Map<String, Object> params = new HashMap<>(requestParameters);
        params.putIfAbsent("temperature", 0.7);
        params.putIfAbsent("max_tokens", 2000);
        return params;
    }
}
```

### 7.5 后续扩展方向

**1. 其他提供商支持**:
- ClaudeExecutor（Anthropic Claude）
- QwenExecutor（阿里云通义千问）
- GeminiExecutor（Google Gemini）

**2. 高级特性**:
- 流式响应支持（SSE）
- 重试机制和错误处理
- 请求限流和负载均衡
- 成本统计和监控

**3. 本地模型支持**:
- LocalModelExecutor 抽象类
- OllamaExecutor（Ollama 本地模型）
- VLLMExecutor（vLLM 推理引擎）

---
