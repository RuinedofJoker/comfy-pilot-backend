# API 设计

## Agent 对话 API（核心）

### 创建对话会话

```
POST /api/v1/conversations
```

**请求体**:
```json
{
  "workflowId": null
}
```
> 新建工作流时 workflowId 为 null，编辑已有工作流时传入工作流 ID

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "conversationId": "123",
    "status": "ACTIVE"
  }
}
```

### 发送消息（创建/编辑工作流）

```
POST /api/v1/conversations/{id}/messages
```

**请求体**:
```json
{
  "role": "USER",
  "content": "创建一个文生图工作流，使用 SDXL 模型",
  "metadata": {
    "action": "CREATE_WORKFLOW"
  }
}
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "messageId": "456",
    "assistantReply": "已创建工作流草稿，包含 SDXL 加载节点...",
    "workflowData": { /* ComfyUI JSON */ }
  }
}
```

### 获取对话历史

```
GET /api/v1/conversations/{id}/messages
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": "456",
      "role": "USER",
      "content": "创建一个文生图工作流",
      "createdAt": "2026-01-14 20:30:45"
    },
    {
      "id": "457",
      "role": "ASSISTANT",
      "content": "已创建工作流草稿...",
      "createdAt": "2026-01-14 20:30:46"
    }
  ]
}
```

## 工作流管理 API

### 查询工作流列表

```
GET /api/v1/workflows?pageNum=1&pageSize=10&status=DRAFT
```

### 获取工作流详情

```
GET /api/v1/workflows/{id}
```

### 激活工作流

```
PUT /api/v1/workflows/{id}/activate
```

**响应**:
```json
{
  "code": 200,
  "message": "工作流已激活",
  "data": {
    "id": "123",
    "status": "ACTIVE"
  }
}
```
