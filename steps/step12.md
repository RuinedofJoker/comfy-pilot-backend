# Step12: Tool模块重构与会话/Agent模块实现准备

> 本文档记录 Step12 完成 Tool 模块的 langchain4j 框架适配重构,并为会话模块和 Agent 模块的实现做准备

## 📋 目录

- [一、Step11 完成情况回顾](#一step11-完成情况回顾)
- [二、Step12 目标](#二step12-目标)
- [三、Tool模块重构详情](#三tool模块重构详情)
- [四、会话模块实现计划](#四会话模块实现计划)
- [五、Agent模块实现计划](#五agent模块实现计划)
- [六、实现文件清单](#六实现文件清单)
- [七、当前进度](#七当前进度)

---

## 一、Step11 完成情况回顾

### 1.1 Step11 原计划内容

Step11 原计划同时实现 Agent 模块和会话模块,但在实际执行过程中发现:

1. **Tool 模块设计需要调整**
   - 原设计包含 ToolExecutor,但与 langchain4j 框架理念冲突
   - langchain4j 框架自身管理工具调用,不需要自定义执行器
   - Tool 模块应该只提供符合 langchain4j 规范的工具类

2. **需求设计文档需要完善**
   - 会话模块设计文档需要补充完整
   - Agent 模块设计文档需要明确代码创建、自动注册的机制

### 1.2 Step11 实际完成内容

Step11 实际完成了以下工作:

1. **完善需求设计文档** ✅
   - 补充会话模块的模块设计、数据模型、API设计
   - 修改 Agent 模块设计,明确代码创建、管理员只能编辑名称描述的规则
   - 修改 Tool 模块设计,适配 langchain4j 框架

2. **重构 Tool 模块代码** ✅
   - 删除 ToolExecutor 相关代码
   - 重写工具类,使用 langchain4j 的 @Tool 注解
   - 修改 ToolRegistry,支持 ToolSpecification 提取
   - 解决循环依赖问题

---

## 二、Step12 目标

### 2.1 核心目标

**完成 Tool 模块重构总结,并开始实现会话模块和 Agent 模块**

### 2.2 设计原则

1. **langchain4j 优先**:所有工具和 Agent 必须符合 langchain4j 规范
2. **代码定义 Agent**:Agent 在代码中创建,启动时自动注册到数据库
3. **管理员权限限制**:管理员只能编辑 Agent 的名称和描述,不能修改代码逻辑
4. **会话驱动交互**:用户通过会话与 Agent 交互
5. **实时通信**:WebSocket 实现实时消息推送

### 2.3 本步骤范围

本 Step 将完成:

1. **Tool 模块重构总结** ✅
   - 记录重构过程和关键决策
   - 记录遇到的问题和解决方案

2. **会话模块实现** ⏳
   - 实现会话管理功能
   - 实现消息管理功能
   - 实现 WebSocket 通信

3. **Agent 模块实现** ⏳
   - 实现 Agent 配置管理
   - 实现 Agent 自动注册机制
   - 实现 Agent 执行引擎
   - 实现具体 Agent(SimpleAgent、WorkflowAgent)

---

## 三、Tool模块重构详情

### 3.1 重构背景

**问题识别**:
- 原 Tool 模块设计包含 ToolExecutor,负责工具的选择和执行
- 这与 langchain4j 框架的设计理念冲突
- langchain4j 框架自身已经提供了完整的工具调用机制

**重构决策**:
- 删除 ToolExecutor 及相关代码
- Tool 模块只提供符合 langchain4j 规范的工具类
- 工具调用由 langchain4j 框架管理

### 3.2 删除的文件

以下文件已被删除:

1. **ToolExecutor.java** - 工具执行器接口
2. **ToolExecutorImpl.java** - 工具执行器实现
3. **Tool.java** - 自定义工具接口
4. **ToolType.java** - 工具类型枚举
5. **ToolExecutionResult.java** - 工具执行结果
6. **ToolExecutionMetadata.java** - 工具执行元数据

**删除原因**: 这些类是为自定义工具执行机制设计的,与 langchain4j 框架重复

### 3.3 修改的核心类

#### 3.3.1 ToolRegistry 接口

**文件**: [ToolRegistry.java](src/main/java/org/joker/comfypilot/tool/domain/service/ToolRegistry.java)

**新增方法**:
```java
// 获取所有工具的 ToolSpecification 列表
List<ToolSpecification> getAllToolSpecifications();

// 根据类名获取工具的 ToolSpecification 列表
List<ToolSpecification> getToolSpecificationsByClassName(String className);

// 根据类名和方法名获取工具的 ToolSpecification
ToolSpecification getToolSpecificationByMethodName(String className, String methodName);
```

**设计目的**: 支持 langchain4j 的 ToolSpecification 系统,提供灵活的工具配置方式

#### 3.3.2 ToolRegistryImpl 实现

**文件**: [ToolRegistryImpl.java](src/main/java/org/joker/comfypilot/tool/infrastructure/service/ToolRegistryImpl.java:120)

**关键修改**:

1. **使用 @PostConstruct 替代 setApplicationContext**
   - **原因**: 避免循环依赖问题
   - **实现**: 在所有 Bean 初始化完成后再扫描工具

```java
@PostConstruct
public void init() {
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    for (String beanName : beanNames) {
        try {
            Object bean = applicationContext.getBean(beanName);
            if (hasToolMethods(bean)) {
                registerTool(bean);
                toolCount++;
            }
        } catch (Exception e) {
            log.debug("跳过 Bean: {}, 原因: {}", beanName, e.getMessage());
        }
    }
}
```

2. **实现 ToolSpecification 提取方法**

```java
@Override
public List<ToolSpecification> getAllToolSpecifications() {
    List<ToolSpecification> allSpecifications = new ArrayList<>();
    for (Object toolBean : toolMap.values()) {
        try {
            List<ToolSpecification> specifications =
                ToolSpecifications.toolSpecificationsFrom(toolBean);
            allSpecifications.addAll(specifications);
        } catch (Exception e) {
            log.warn("无法从工具实例提取 ToolSpecification: {}",
                toolBean.getClass().getSimpleName());
        }
    }
    return allSpecifications;
}
```

#### 3.3.3 LlmTool 重写

**文件**: [LlmTool.java](src/main/java/org/joker/comfypilot/tool/infrastructure/tool/LlmTool.java)

**重写要点**:

1. **删除 Tool 接口实现**
2. **使用 @Tool 和 @P 注解**
3. **方法返回 String 类型**

```java
@Tool("使用大语言模型生成文本内容")
public String generateText(
        @P("输入的提示文本") String prompt,
        @P("温度参数，控制输出的随机性，范围 0-2") Double temperature,
        @P("最大生成的 token 数量") Integer maxTokens) {

    try {
        // 调用 ModelCapabilityService
        ModelCapabilityResponse response = modelCapabilityService.invoke(request);
        return response.getResult().toString();
    } catch (Exception e) {
        return "错误：文本生成失败 - " + e.getMessage();
    }
}
```

#### 3.3.4 EmbeddingTool 重写

**文件**: [EmbeddingTool.java](src/main/java/org/joker/comfypilot/tool/infrastructure/tool/EmbeddingTool.java)

**重写要点**:

1. **简化为单一方法**
2. **使用 @Tool 注解**
3. **返回简洁的结果描述**

```java
@Tool("将文本转换为向量表示")
public String generateEmbedding(@P("需要向量化的文本") String text) {
    try {
        ModelCapabilityResponse response = modelCapabilityService.invoke(request);
        Object result = response.getResult();

        if (result instanceof List) {
            List<Double> embedding = (List<Double>) result;
            return "向量生成成功，维度: " + embedding.size();
        }
        return "向量生成成功";
    } catch (Exception e) {
        return "错误：向量生成失败 - " + e.getMessage();
    }
}
```

### 3.4 遇到的问题与解决方案

#### 问题1: 循环依赖异常

**错误信息**:
```
BeanCurrentlyInCreationException: Error creating bean with name 'toolRegistryImpl':
Requested bean is currently in creation: Is there an unresolvable circular reference?
```

**问题原因**:
- 在 `setApplicationContext()` 方法中调用 `applicationContext.getBean()`
- 此时 Bean 还在创建过程中,导致循环依赖

**解决方案**:
- 将工具扫描逻辑从 `setApplicationContext()` 移到 `@PostConstruct` 方法
- `@PostConstruct` 在所有 Bean 初始化完成后执行,避免循环依赖

**代码对比**:
```java
// ❌ 错误做法
@Override
public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    // 立即扫描会导致循环依赖
    scanAndRegisterTools();
}

// ✅ 正确做法
@Override
public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
}

@PostConstruct
public void init() {
    // 延迟到所有 Bean 初始化完成后再扫描
    scanAndRegisterTools();
}
```

### 3.5 重构总结

**重构成果**:
1. ✅ 删除了 6 个不必要的类和接口
2. ✅ ToolRegistry 新增 3 个 ToolSpecification 相关方法
3. ✅ ToolRegistryImpl 使用 @PostConstruct 解决循环依赖
4. ✅ LlmTool 和 EmbeddingTool 完全符合 langchain4j 规范
5. ✅ Tool 模块架构更加简洁清晰

**设计优势**:
- **框架集成**: 完全符合 langchain4j 规范,工具调用由框架管理
- **开发简单**: 只需编写带 @Tool 注解的方法,自动注册
- **可扩展性**: 易于添加新工具,工具之间相互独立
- **类型安全**: 编译时类型检查,IDE 自动补全支持

---

## 四、会话模块实现计划

### 4.1 模块概述

会话模块负责管理用户与 Agent 的对话会话,包括:
- 会话生命周期管理
- 消息收发和历史记录
- WebSocket 实时通信

### 4.2 数据模型

#### 4.2.1 chat_session 表

```sql
CREATE TABLE chat_session (
    id BIGSERIAL PRIMARY KEY,
    session_code VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    agent_id BIGINT NOT NULL,
    title VARCHAR(200),
    status VARCHAR(20) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL,
    update_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

#### 4.2.2 chat_message 表

```sql
CREATE TABLE chat_message (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB,
    create_time TIMESTAMP NOT NULL,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL,
    update_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

### 4.3 核心类设计

#### 4.3.1 领域层（domain/）
- **ChatSession** - 会话实体,包含会话状态管理方法
- **ChatMessage** - 消息实体,支持用户消息和 Agent 消息
- **SessionStatus** - 会话状态枚举(ACTIVE, CLOSED)
- **MessageRole** - 消息角色枚举(USER, ASSISTANT, SYSTEM)

#### 4.3.2 应用层（application/）
- **ChatSessionService** - 会话管理服务
  - 创建会话
  - 发送消息(调用 Agent 执行)
  - 查询会话历史
  - 关闭会话

#### 4.3.3 基础设施层（infrastructure/）
- **ChatSessionPO/ChatMessagePO** - 持久化对象
- **ChatSessionMapper/ChatMessageMapper** - MyBatis Mapper
- **ChatSessionRepositoryImpl** - 仓储实现

#### 4.3.4 接口层（interfaces/）
- **ChatSessionController** - REST API 控制器
- **ChatWebSocketHandler** - WebSocket 处理器

---

## 五、Agent模块实现计划

### 5.1 模块概述

Agent 模块负责:
- Agent 配置管理(仅名称和描述可编辑)
- Agent 自动注册(代码定义,启动时同步到数据库)
- Agent 执行引擎(基于 langchain4j)

### 5.2 核心设计原则

**代码定义 Agent**:
- Agent 在代码中实现,继承 Agent 接口
- 每个 Agent 提供元数据方法(getAgentCode, getAgentName, getDescription)
- 启动时 AgentRegistry 自动扫描并注册到数据库

**管理员权限限制**:
- 管理员只能通过 API 编辑 Agent 的名称和描述
- Agent 的代码逻辑、系统提示词、配置参数不可通过 API 修改

### 5.3 数据模型

#### 5.3.1 agent_config 表

```sql
CREATE TABLE agent_config (
    id BIGSERIAL PRIMARY KEY,
    agent_code VARCHAR(50) UNIQUE NOT NULL,
    agent_name VARCHAR(100) NOT NULL,
    agent_type VARCHAR(50) NOT NULL,
    description TEXT,
    system_prompt TEXT,
    config JSONB,
    status VARCHAR(20) NOT NULL,
    create_time TIMESTAMP NOT NULL,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL,
    update_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

#### 5.3.2 agent_execution_log 表

```sql
CREATE TABLE agent_execution_log (
    id BIGSERIAL PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    session_id BIGINT NOT NULL,
    input TEXT NOT NULL,
    output TEXT,
    status VARCHAR(20) NOT NULL,
    error_message TEXT,
    execution_time_ms BIGINT,
    create_time TIMESTAMP NOT NULL,
    create_by BIGINT,
    update_time TIMESTAMP NOT NULL,
    update_by BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE
);
```

### 5.4 核心类设计

#### 5.4.1 Agent 接口

```java
public interface Agent {
    // 元数据方法
    String getAgentCode();
    String getAgentName();
    String getDescription();
    String getSystemPrompt();

    // 执行方法
    AgentExecutionResponse execute(AgentExecutionRequest request);
}
```

#### 5.4.2 AgentRegistry

**职责**: Agent 注册中心
- 启动时扫描所有 Agent 实现类
- 同步 Agent 元数据到数据库
- 提供 Agent 实例查找功能

#### 5.4.3 AgentExecutor

**职责**: Agent 执行器
- 根据 agentCode 查找 Agent 实例
- 调用 Agent 执行
- 记录执行日志

#### 5.4.4 具体 Agent 实现

- **SimpleAgent** - 简单对话 Agent,使用 LlmTool
- **WorkflowAgent** - 工作流编辑 Agent,支持工具调用

---

## 六、实现文件清单

### 6.1 会话模块文件（19个）

#### 领域层（domain/）
- ⏳ ChatSession.java - 会话实体
- ⏳ ChatMessage.java - 消息实体
- ⏳ SessionStatus.java - 会话状态枚举
- ⏳ MessageRole.java - 消息角色枚举
- ⏳ ChatSessionRepository.java - 会话仓储接口
- ⏳ ChatMessageRepository.java - 消息仓储接口

#### 应用层（application/）
- ⏳ ChatSessionService.java - 会话服务接口
- ⏳ ChatSessionServiceImpl.java - 会话服务实现
- ⏳ ChatSessionDTO.java - 会话 DTO
- ⏳ ChatMessageDTO.java - 消息 DTO
- ⏳ SendMessageRequest.java - 发送消息请求
- ⏳ ChatSessionDTOConverter.java - DTO 转换器

#### 基础设施层（infrastructure/）
- ⏳ ChatSessionPO.java - 会话 PO
- ⏳ ChatMessagePO.java - 消息 PO
- ⏳ ChatSessionMapper.java - 会话 Mapper
- ⏳ ChatMessageMapper.java - 消息 Mapper
- ⏳ ChatSessionRepositoryImpl.java - 会话仓储实现
- ⏳ ChatMessageRepositoryImpl.java - 消息仓储实现
- ⏳ ChatSessionConverter.java - PO 转换器

#### 接口层（interfaces/）
- ⏳ ChatSessionController.java - 会话控制器
- ⏳ ChatWebSocketHandler.java - WebSocket 处理器

### 6.2 Agent模块文件（28个）

#### 领域层（domain/）
- ⏳ AgentConfig.java - Agent 配置实体
- ⏳ AgentExecutionLog.java - 执行日志实体
- ⏳ AgentType.java - Agent 类型枚举
- ⏳ AgentStatus.java - Agent 状态枚举
- ⏳ ExecutionStatus.java - 执行状态枚举
- ⏳ Agent.java - Agent 接口
- ⏳ AgentRegistry.java - Agent 注册中心接口
- ⏳ AgentConfigRepository.java - Agent 配置仓储接口
- ⏳ AgentExecutionLogRepository.java - 执行日志仓储接口

#### 应用层（application/）
- ⏳ AgentExecutor.java - Agent 执行器接口
- ⏳ AgentExecutorImpl.java - Agent 执行器实现
- ⏳ AgentConfigService.java - Agent 配置服务接口
- ⏳ AgentConfigServiceImpl.java - Agent 配置服务实现
- ⏳ AgentConfigDTO.java - Agent 配置 DTO
- ⏳ AgentExecutionRequest.java - 执行请求
- ⏳ AgentExecutionResponse.java - 执行响应
- ⏳ AgentConfigDTOConverter.java - DTO 转换器

#### 基础设施层（infrastructure/）
- ⏳ AgentConfigPO.java - Agent 配置 PO
- ⏳ AgentExecutionLogPO.java - 执行日志 PO
- ⏳ AgentConfigMapper.java - Agent 配置 Mapper
- ⏳ AgentExecutionLogMapper.java - 执行日志 Mapper
- ⏳ AgentConfigRepositoryImpl.java - Agent 配置仓储实现
- ⏳ AgentExecutionLogRepositoryImpl.java - 执行日志仓储实现
- ⏳ AgentConfigConverter.java - PO 转换器
- ⏳ AgentRegistryImpl.java - Agent 注册中心实现
- ⏳ SimpleAgent.java - 简单对话 Agent
- ⏳ WorkflowAgent.java - 工作流编辑 Agent

#### 接口层（interfaces/）
- ⏳ AgentConfigController.java - Agent 配置控制器

**总计预计文件数**: 47 个

---

## 七、当前进度

### 7.1 完成度统计

**总体进度**: 10% ⏳

**模块完成度**:
- [x] Tool 模块重构 - 100% ✅
- [ ] 会话模块实现 - 0% ⏳
- [ ] Agent 模块实现 - 0% ⏳

### 7.2 已完成任务

1. ✅ 完善会话模块需求设计文档
2. ✅ 完善 Agent 模块需求设计文档
3. ✅ 修改 Tool 模块设计文档
4. ✅ 重构 Tool 模块代码
5. ✅ 创建 Step12 文档

### 7.3 待完成任务

1. ⏳ 实现会话模块代码（Entity、PO、Repository、Service、Controller、WebSocket）
2. ⏳ 实现 Agent 模块代码（Entity、PO、Repository、Service、Agent实现）
3. ⏳ 集成 langchain4j 框架实现 Agent 流程
4. ⏳ 测试会话模块与 Agent 模块的完整链路

### 7.4 下一步计划

**优先级1**: 实现会话模块
- 创建领域实体和枚举
- 实现持久化层
- 实现应用服务层
- 实现 REST API 和 WebSocket

**优先级2**: 实现 Agent 模块
- 创建 Agent 接口和基础类
- 实现 AgentRegistry 自动注册机制
- 实现 AgentExecutor 执行引擎
- 实现具体 Agent(SimpleAgent、WorkflowAgent)

**优先级3**: 集成测试
- 测试会话创建和消息发送
- 测试 Agent 执行流程
- 测试 WebSocket 实时通信
- 测试完整的用户对话链路

---

**Step12 状态**: ⏳ 进行中

**创建时间**: 2026-01-17

**最后更新**: 2026-01-17

---
