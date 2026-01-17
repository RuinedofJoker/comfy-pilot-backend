# 会话模块 - API设计

> 本文档定义会话模块的API接口

## 1. 接口概览

会话模块提供以下接口：

### 1.1 REST API
- 会话管理接口
- 消息管理接口

### 1.2 WebSocket API
- 实时消息推送接口

---

## 2. 会话管理接口

### 2.1 创建会话

**接口路径**：`POST /api/v1/sessions`

**请求参数**：
```json
{
  "agentId": 1,
  "title": "工作流编辑会话"
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| agentId | Long | 是 | Agent ID |
| title | String | 否 | 会话标题 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionCode": "SESSION_20260117_001",
    "agentId": 1,
    "title": "工作流编辑会话",
    "status": "ACTIVE",
    "createTime": "2026-01-17T10:00:00Z",
    "updateTime": "2026-01-17T10:00:00Z"
  }
}
```

---

### 2.2 获取会话详情

**接口路径**：`GET /api/v1/sessions/{id}`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 会话ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionCode": "SESSION_20260117_001",
    "agentId": 1,
    "title": "工作流编辑会话",
    "status": "ACTIVE",
    "createTime": "2026-01-17T10:00:00Z",
    "updateTime": "2026-01-17T10:00:00Z"
  }
}
```

---

### 2.3 查询会话列表

**接口路径**：`GET /api/v1/sessions`

**查询参数**：
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| status | String | 否 | - | 会话状态 |
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
        "sessionCode": "SESSION_20260117_001",
        "agentId": 1,
        "title": "工作流编辑会话",
        "status": "ACTIVE",
        "createTime": "2026-01-17T10:00:00Z",
        "updateTime": "2026-01-17T10:00:00Z"
      }
    ]
  }
}
```

---

### 2.4 更新会话

**接口路径**：`PUT /api/v1/sessions/{id}`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 会话ID |

**请求参数**：
```json
{
  "title": "新的会话标题",
  "agentId": 2
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 否 | 会话标题 |
| agentId | Long | 否 | Agent ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionCode": "SESSION_20260117_001",
    "agentId": 2,
    "title": "新的会话标题",
    "status": "ACTIVE",
    "createTime": "2026-01-17T10:00:00Z",
    "updateTime": "2026-01-17T10:30:00Z"
  }
}
```

---

### 2.5 删除会话

**接口路径**：`DELETE /api/v1/sessions/{id}`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 会话ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 2.6 归档会话

**接口路径**：`POST /api/v1/sessions/{id}/archive`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 会话ID |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionCode": "SESSION_20260117_001",
    "agentId": 1,
    "title": "工作流编辑会话",
    "status": "ARCHIVED",
    "createTime": "2026-01-17T10:00:00Z",
    "updateTime": "2026-01-17T11:00:00Z"
  }
}
```

---

## 3. 消息管理接口

### 3.1 发送消息

**接口路径**：`POST /api/v1/sessions/{sessionId}/messages`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**请求参数**：
```json
{
  "content": "请帮我创建一个图像生成工作流"
}
```

**参数说明**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| content | String | 是 | 消息内容 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userMessage": {
      "id": 1,
      "sessionId": 1,
      "role": "USER",
      "content": "请帮我创建一个图像生成工作流",
      "createTime": "2026-01-17T10:00:00Z"
    },
    "assistantMessage": {
      "id": 2,
      "sessionId": 1,
      "role": "ASSISTANT",
      "content": "好的，我来帮你创建一个图像生成工作流...",
      "metadata": {
        "model": "gpt-4",
        "inputTokens": 20,
        "outputTokens": 100,
        "executionTimeMs": 1500
      },
      "createTime": "2026-01-17T10:00:02Z"
    }
  }
}
```

---

### 3.2 获取消息历史

**接口路径**：`GET /api/v1/sessions/{sessionId}/messages`

**路径参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| sessionId | Long | 是 | 会话ID |

**查询参数**：
| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| role | String | 否 | - | 消息角色 |
| page | Integer | 否 | 1 | 页码 |
| size | Integer | 否 | 50 | 每页数量 |

**响应示例**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "page": 1,
    "size": 50,
    "items": [
      {
        "id": 1,
        "sessionId": 1,
        "role": "USER",
        "content": "请帮我创建一个图像生成工作流",
        "createTime": "2026-01-17T10:00:00Z"
      },
      {
        "id": 2,
        "sessionId": 1,
        "role": "ASSISTANT",
        "content": "好的，我来帮你创建一个图像生成工作流...",
        "metadata": {
          "model": "gpt-4",
          "inputTokens": 20,
          "outputTokens": 100,
          "executionTimeMs": 1500
        },
        "createTime": "2026-01-17T10:00:02Z"
      }
    ]
  }
}
```

---

## 4. WebSocket 接口

### 4.1 连接建立

**连接地址**：`ws://localhost:8080/ws/chat`

**连接参数**：
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| token | String | 是 | 认证Token（TODO: 待集成认证模块） |

**连接示例**：
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/chat?token=YOUR_TOKEN');

ws.onopen = () => {
  console.log('WebSocket 连接已建立');
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log('收到消息:', message);
};
```

---

### 4.2 开始会话

**消息类型**：`START_SESSION`

**客户端发送**：
```json
{
  "type": "START_SESSION",
  "data": {
    "agentId": 1,
    "title": "工作流编辑会话"
  },
  "timestamp": 1705478400000
}
```

**字段说明**：
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 消息类型，固定为 START_SESSION |
| data.agentId | Long | 是 | Agent ID |
| data.title | String | 否 | 会话标题 |
| timestamp | Long | 是 | 时间戳（毫秒） |

**服务端响应**：
```json
{
  "type": "SESSION_CREATED",
  "sessionCode": "session_abc123def456",
  "timestamp": 1705478400100
}
```

---

### 4.3 发送用户消息

**消息类型**：`USER_MESSAGE`

**客户端发送**：
```json
{
  "type": "USER_MESSAGE",
  "sessionCode": "session_abc123def456",
  "content": "请帮我创建一个图像生成工作流",
  "timestamp": 1705478400000
}
```

**字段说明**：
| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| type | String | 是 | 消息类型，固定为 USER_MESSAGE |
| sessionCode | String | 是 | 会话编码 |
| content | String | 是 | 消息内容 |
| timestamp | Long | 是 | 时间戳（毫秒） |

---

### 4.4 Agent 流式输出消息

#### 4.4.1 Agent 思考中

**消息类型**：`AGENT_THINKING`

**服务端推送**：
```json
{
  "type": "AGENT_THINKING",
  "sessionCode": "session_abc123def456",
  "content": "Agent正在思考中...",
  "timestamp": 1705478400200
}
```

#### 4.4.2 Agent 流式输出

**消息类型**：`AGENT_STREAM`

**服务端推送**：
```json
{
  "type": "AGENT_STREAM",
  "sessionCode": "session_abc123def456",
  "content": "好的，我来帮你",
  "timestamp": 1705478400300
}
```

**说明**：Agent 会多次推送此类型消息，每次包含部分内容。

#### 4.4.3 Agent 调用工具

**消息类型**：`AGENT_TOOL_CALL`

**服务端推送**：
```json
{
  "type": "AGENT_TOOL_CALL",
  "sessionCode": "session_abc123def456",
  "content": "createWorkflow",
  "data": {
    "toolName": "createWorkflow",
    "toolArgs": "{\"name\":\"图像生成工作流\"}"
  },
  "timestamp": 1705478400400
}
```

#### 4.4.4 Agent 请求用户输入

**消息类型**：`AGENT_REQUEST_INPUT`

**服务端推送**：
```json
{
  "type": "AGENT_REQUEST_INPUT",
  "sessionCode": "session_abc123def456",
  "content": "请提供图像的尺寸（宽x高）",
  "timestamp": 1705478400500
}
```

**客户端响应**：
```json
{
  "type": "USER_RESPONSE",
  "sessionCode": "session_abc123def456",
  "content": "512x512",
  "timestamp": 1705478410000
}
```

#### 4.4.5 Agent 执行完成

**消息类型**：`AGENT_COMPLETE`

**服务端推送**：
```json
{
  "type": "AGENT_COMPLETE",
  "sessionCode": "session_abc123def456",
  "content": "好的，我来帮你创建一个图像生成工作流。我已经创建了一个基础的工作流模板...",
  "timestamp": 1705478420000
}
```

---

### 4.5 中断执行

**消息类型**：`INTERRUPT`

**客户端发送**：
```json
{
  "type": "INTERRUPT",
  "sessionCode": "session_abc123def456",
  "timestamp": 1705478415000
}
```

**服务端响应**：
```json
{
  "type": "EXECUTION_INTERRUPTED",
  "content": "执行已中断",
  "timestamp": 1705478415100
}
```

---

### 4.6 心跳保活

**消息类型**：`PING` / `PONG`

**客户端发送**：
```json
{
  "type": "PING",
  "timestamp": 1705478430000
}
```

**服务端响应**：
```json
{
  "type": "PONG",
  "timestamp": 1705478430100
}
```

**说明**：建议客户端每 30 秒发送一次心跳。

---

### 4.7 错误消息

**消息类型**：`ERROR`

**服务端推送**：
```json
{
  "type": "ERROR",
  "error": "会话不存在",
  "timestamp": 1705478440000
}
```

**常见错误**：
- "会话不存在" - sessionCode 无效
- "会话已关闭,无法发送消息" - 会话状态为 ARCHIVED
- "当前会话正在执行中,请稍后再试" - 同一连接已有 Agent 在执行
- "处理消息失败: ..." - 其他业务错误

---

## 5. 错误码

| 错误码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| SESSION_NOT_FOUND | 404 | 会话不存在 |
| SESSION_ARCHIVED | 400 | 会话已归档 |
| AGENT_NOT_FOUND | 404 | Agent不存在 |
| MESSAGE_EMPTY | 400 | 消息内容为空 |
| UNAUTHORIZED | 401 | 未授权 |
| FORBIDDEN | 403 | 无权限 |

---
