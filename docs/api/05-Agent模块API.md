# Agent模块 API 文档

## 基础信息
- 基础路径: `/api/v1/agents`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer {token}`

---

## 1. Agent配置管理

### 1.1 创建Agent配置
**接口**: `POST /agents`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",              // Agent名称，必填
  "type": "string",              // Agent类型，必填
  "description": "string",       // 描述，可选
  "modelId": 1,                  // 模型ID，必填
  "systemPrompt": "string",      // 系统提示词，必填
  "temperature": 0.7,            // 温度参数，可选
  "maxTokens": 4096,             // 最大Token数，可选
  "toolsConfig": ""            // 工具配置JSON，可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "工作流编辑助手",
    "type": "WORKFLOW_EDITOR",
    "description": "帮助用户编辑工作流",
    "modelId": 1,
    "systemPrompt": "你是一个工作流编辑助手...",
    "temperature": 0.7,
    "maxTokens": 4096,
    "toolsConfig": "{}",
    "status": "ACTIVE",
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 更新Agent配置
**接口**: `PUT /agents/{id}`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",
  "description": "string",
  "systemPrompt": "string",
  "temperature": 0.7,
  "maxTokens": 4096,
  "toolsConfig": "{}"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "更新后的Agent",
    "temperature": 0.8,
    "updateTime": "2026-01-15T11:00:00"
  }
}
```

---

### 1.3 获取Agent配置
**接口**: `GET /agents/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "工作流编辑助手",
    "type": "WORKFLOW_EDITOR",
    "description": "帮助用户编辑工作流",
    "modelId": 1,
    "systemPrompt": "你是一个工作流编辑助手...",
    "temperature": 0.7,
    "maxTokens": 4096,
    "toolsConfig": "{}",
    "status": "ACTIVE"
  }
}
```

---

### 1.4 获取所有激活的Agent
**接口**: `GET /agents`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "工作流编辑助手",
      "type": "WORKFLOW_EDITOR",
      "status": "ACTIVE"
    }
  ]
}
```

---

### 1.5 激活Agent
**接口**: `POST /agents/{id}/activate`

**权限**: 管理员

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.6 停用Agent
**接口**: `POST /agents/{id}/deactivate`

**权限**: 管理员

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.7 删除Agent
**接口**: `DELETE /agents/{id}`

**权限**: 管理员

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 枚举值说明

### AgentType (Agent类型)
- `WORKFLOW_EDITOR`: 工作流编辑器
- `WORKFLOW_ANALYZER`: 工作流分析器
- `GENERAL_ASSISTANT`: 通用助手

### AgentStatus (Agent状态)
- `ACTIVE`: 激活
- `INACTIVE`: 未激活
- `MAINTENANCE`: 维护中

### ExecutionType (执行类型)
- `WORKFLOW_EDIT`: 工作流编辑
- `WORKFLOW_ANALYZE`: 工作流分析
- `CHAT_RESPONSE`: 聊天响应

### ExecutionStatus (执行状态)
- `PENDING`: 等待中
- `RUNNING`: 运行中
- `SUCCESS`: 成功
- `FAILED`: 失败
- `TIMEOUT`: 超时
