# Agent模块 - API设计

> 本文档定义Agent模块的API接口

## 1. 接口概览

Agent 模块提供以下接口：

### 1.1 Agent配置管理接口
- Agent配置查询（详情、列表）
- Agent配置更新（仅名称和描述）
- Agent状态管理（激活、停用）
- Agent配置删除

**重要说明**：
- Agent 只能通过代码创建，应用启动时自动注册
- 管理员只能通过 API 修改 Agent 的名称和描述
- Agent 的类型、系统提示词、配置等由代码定义，不可通过 API 修改

### 1.2 Agent执行接口
- 执行Agent（内部接口）
- 查询执行日志

---

## 2. Agent配置管理接口

**重要提示**：Agent 通过代码创建并在应用启动时自动注册，不提供创建 API。

---

### 2.1 获取Agent配置详情

**接口路径**：`GET /api/v1/agents/{id}`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | Agent ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "agentCode": "SIMPLE_AGENT_001",
    "agentName": "简单对话Agent",
    "agentType": "SIMPLE",
    "description": "用于简单对话交互的Agent",
    "systemPrompt": "你是一个友好的AI助手",
    "config": {
      "temperature": 0.7,
      "maxTokens": 2000,
      "modelId": 1
    },
    "status": "ACTIVE",
    "createTime": "2026-01-17T10:00:00Z",
    "updateTime": "2026-01-17T10:00:00Z"
  }
}
```

---

### 2.2 查询Agent配置列表

**接口路径**：`GET /api/v1/agents`

**查询参数**：
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| agentType | String | 否 | - | Agent类型 |
| status | String | 否 | - | Agent状态 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 20 | 每页数量 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 1,
        "agentCode": "SIMPLE_AGENT_001",
        "agentName": "简单对话Agent",
        "agentType": "SIMPLE",
        "status": "ACTIVE",
        "createTime": "2026-01-17T10:00:00Z"
      }
    ]
  }
}
```


---

### 2.3 更新Agent配置

**接口路径**：`PUT /api/v1/agents/{id}`

**重要说明**：此接口仅允许更新 Agent 的名称和描述，其他字段（agentType、systemPrompt、config）由代码定义，不可修改。

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | Agent ID |

**请求参数**：
```json
{
  "agentName": "新的Agent名称",
  "description": "新的描述"
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| agentName | String | 否 | Agent名称 |
| description | String | 否 | Agent描述 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "agentCode": "SIMPLE_AGENT_001",
    "agentName": "新的Agent名称",
    "agentType": "SIMPLE",
    "description": "新的描述",
    "systemPrompt": "你是一个友好的AI助手",
    "config": {
      "temperature": 0.7,
      "maxTokens": 2000,
      "modelId": 1
    },
    "status": "ACTIVE",
    "createTime": "2026-01-17T10:00:00Z",
    "updateTime": "2026-01-17T10:30:00Z"
  }
}
```

**注意**：响应中的 systemPrompt 和 config 字段保持不变，仅 agentName 和 description 被更新。

---

### 2.4 删除Agent配置

**接口路径**：`DELETE /api/v1/agents/{id}`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | Agent ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```


---

## 3. Agent执行接口（内部接口）

### 3.1 执行Agent

**说明**：此接口为内部接口，由会话模块调用，不对外暴露REST API。

**接口定义**：
```java
public interface AgentExecutor {
    AgentExecutionResponse execute(Long agentId, String input, Long sessionId);
}
```

**请求参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| agentId | Long | 是 | Agent ID |
| input | String | 是 | 输入内容 |
| sessionId | Long | 是 | 会话ID |

**响应对象**：
```java
public class AgentExecutionResponse {
    private boolean success;
    private String output;
    private String errorMessage;
    private AgentExecutionMetadata metadata;
}
```

---

### 3.2 查询执行日志

**接口路径**：`GET /api/v1/agents/{agentId}/logs`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| agentId | Long | 是 | Agent ID |

**查询参数**：
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| sessionId | Long | 否 | - | 会话ID |
| status | String | 否 | - | 执行状态 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 20 | 每页数量 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 1,
        "agentId": 1,
        "sessionId": 1,
        "input": "你好",
        "output": "你好！我是AI助手",
        "status": "SUCCESS",
        "executionTimeMs": 1200,
        "createTime": "2026-01-17T10:00:00Z"
      }
    ]
  }
}
```

---

## 4. 错误码

| 错误码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| AGENT_NOT_FOUND | 404 | Agent不存在 |
| AGENT_INACTIVE | 400 | Agent已停用 |
| AGENT_TYPE_INVALID | 400 | Agent类型无效 |
| AGENT_CODE_EXISTS | 400 | Agent编码已存在 |
| EXECUTION_FAILED | 500 | Agent执行失败 |

---
