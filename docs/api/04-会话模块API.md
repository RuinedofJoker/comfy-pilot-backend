# 会话模块 API 文档

## 基础信息
- 基础路径: `/api/v1/sessions`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer {token}`

---

## 1. 会话管理

### 1.1 创建会话
**接口**: `POST /sessions`

**请求体**:
```json
{
  "workflowId": 1,              // 工作流ID，必填
  "title": "string"             // 会话标题，必填
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "workflowId": 1,
    "title": "新会话",
    "status": "ACTIVE",
    "lastMessageSummary": null,
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 获取会话列表
**接口**: `GET /sessions`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10
- `workflowId`: 工作流ID筛选，可选
- `status`: 状态筛选，可选

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 15,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": 1,
        "workflowId": 1,
        "title": "我的会话",
        "status": "ACTIVE",
        "lastMessageSummary": "添加了一个新节点",
        "createTime": "2026-01-15T10:00:00",
        "updateTime": "2026-01-15T10:30:00"
      }
    ]
  }
}
```

---

### 1.3 获取会话详情
**接口**: `GET /sessions/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "workflowId": 1,
    "title": "我的会话",
    "status": "ACTIVE",
    "lastMessageSummary": "添加了一个新节点",
    "createTime": "2026-01-15T10:00:00",
    "updateTime": "2026-01-15T10:30:00"
  }
}
```

---

### 1.4 更新会话
**接口**: `PUT /sessions/{id}`

**请求体**:
```json
{
  "title": "string",
  "status": "ACTIVE|COMPLETED|ARCHIVED"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "更新后的标题",
    "status": "COMPLETED",
    "updateTime": "2026-01-15T11:00:00"
  }
}
```

---

### 1.5 删除会话
**接口**: `DELETE /sessions/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 2. 消息管理

### 2.1 发送消息
**接口**: `POST /sessions/{sessionId}/messages`

**请求体**:
```json
{
  "content": "string"           // 消息内容，必填
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "sessionId": 1,
    "role": "USER",
    "content": "请帮我添加一个图像加载节点",
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 2.2 获取会话消息列表
**接口**: `GET /sessions/{sessionId}/messages`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认20

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 50,
    "page": 1,
    "size": 20,
    "items": [
      {
        "id": 1,
        "sessionId": 1,
        "role": "USER",
        "content": "请帮我添加一个图像加载节点",
        "createTime": "2026-01-15T10:00:00"
      },
      {
        "id": 2,
        "sessionId": 1,
        "role": "ASSISTANT",
        "content": "好的，我已经为您添加了图像加载节点",
        "createTime": "2026-01-15T10:00:05"
      }
    ]
  }
}
```

---

## 枚举值说明

### SessionStatus (会话状态)
- `ACTIVE`: 活跃
- `COMPLETED`: 已完成
- `ARCHIVED`: 已归档

### MessageRole (消息角色)
- `USER`: 用户
- `ASSISTANT`: 助手
- `SYSTEM`: 系统
