    # API 接口设计

## 1. 认证接口

### 1.1 用户登录
**接口**: `POST /api/v1/auth/login`

**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string",
    "user": {
      "id": 1,
      "username": "string",
      "displayName": "string",
      "role": "USER|ADMIN"
    }
  }
}
```

### 1.2 用户登出
**接口**: `POST /api/v1/auth/logout`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
  "code": 200,
  "message": "success"
}
```

### 1.3 刷新Token
**接口**: `POST /api/v1/auth/refresh`

**请求头**: `Authorization: Bearer {token}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string"
  }
}
```

## 2. ComfyUI 服务管理接口

### 2.1 获取服务列表
**接口**: `GET /api/v1/services`

**请求参数**: 无

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "生产环境",
      "url": "http://192.168.1.100:8188",
      "description": "高性能GPU服务器",
      "status": "ONLINE",
      "workflowCount": 10,
      "sessionCount": 5,
      "lastCheckTime": "2025-01-15T10:00:00Z"
    }
  ]
}
```

### 2.2 创建服务
**接口**: `POST /api/v1/services`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",
  "url": "string",
  "description": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1
  }
}
```

### 2.3 更新服务
**接口**: `PUT /api/v1/services/{id}`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",
  "url": "string",
  "description": "string"
}
```

### 2.4 删除服务
**接口**: `DELETE /api/v1/services/{id}`

**权限**: 管理员

### 2.5 测试服务连接
**接口**: `POST /api/v1/services/{id}/test`

**权限**: 管理员

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "ONLINE",
    "responseTime": 150
  }
}
```

## 3. 工作流管理接口

### 3.1 获取工作流列表
**接口**: `GET /api/v1/workflows`

**请求参数**:
- `serviceId` (可选): 按服务筛选
- `favorite` (可选): 是否收藏
- `search` (可选): 搜索关键词
- `sortBy` (可选): 排序字段 (lastUsed|name|createdAt|updatedAt)
- `sortOrder` (可选): 排序方向 (asc|desc)

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "SDXL图像生成",
      "description": "描述",
      "serviceId": 1,
      "serviceName": "生产环境",
      "isFavorite": true,
      "sessionCount": 3,
      "versionCount": 5,
      "lastUsedAt": "2025-01-15T10:00:00Z"
    }
  ]
}
```

### 3.2 创建工作流
**接口**: `POST /api/v1/workflows`

**请求体**:
```json
{
  "name": "string",
  "description": "string",
  "serviceId": 1,
  "workflowData": {}
}
```

### 3.3 更新工作流
**接口**: `PUT /api/v1/workflows/{id}`

**请求体**:
```json
{
  "name": "string",
  "description": "string",
  "workflowData": {}
}
```

### 3.4 删除工作流
**接口**: `DELETE /api/v1/workflows/{id}`

### 3.5 切换收藏状态
**接口**: `POST /api/v1/workflows/{id}/favorite`

**请求体**:
```json
{
  "favorite": true
}
```

## 4. 会话管理接口

### 4.1 获取会话列表
**接口**: `GET /api/v1/sessions`

**请求参数**:
- `workflowId` (可选): 按工作流筛选

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "SDXL优化讨论",
      "workflowId": 1,
      "status": "ACTIVE",
      "lastMessageSummary": "调整采样步数",
      "updatedAt": "2025-01-15T11:00:00Z"
    }
  ]
}
```

### 4.2 创建会话
**接口**: `POST /api/v1/sessions`

**请求体**:
```json
{
  "workflowId": 1,
  "title": "string"
}
```

### 4.3 获取会话消息
**接口**: `GET /api/v1/sessions/{id}/messages`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "role": "USER",
      "content": "帮我优化这个工作流",
      "createdAt": "2025-01-15T10:00:00Z"
    }
  ]
}
```

### 4.4 发送消息
**接口**: `POST /api/v1/sessions/{id}/messages`

**请求体**:
```json
{
  "content": "string"
}
```

## 5. 用户管理接口

### 5.1 获取当前用户信息
**接口**: `GET /api/v1/users/me`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "user",
    "displayName": "张三",
    "email": "user@example.com",
    "avatarUrl": "https://...",
    "role": "USER",
    "workflowCount": 10,
    "sessionCount": 5,
    "createdAt": "2025-01-01T00:00:00Z"
  }
}
```

### 5.2 更新用户信息
**接口**: `PUT /api/v1/users/me`

**请求体**:
```json
{
  "displayName": "string",
  "email": "string",
  "avatarUrl": "string"
}
```

### 5.3 修改密码
**接口**: `PUT /api/v1/users/me/password`

**请求体**:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

## 6. WebSocket 接口

### 6.1 连接地址
**地址**: `ws://host:port/ws/chat?token={jwt_token}`

### 6.2 消息格式

**客户端发送**:
```json
{
  "type": "SEND_MESSAGE",
  "sessionId": 1,
  "content": "string"
}
```

**服务端推送**:
```json
{
  "type": "MESSAGE_RECEIVED",
  "sessionId": 1,
  "message": {
    "id": 1,
    "role": "ASSISTANT",
    "content": "string",
    "createdAt": "2025-01-15T10:00:00Z"
  }
}
```

**工作流锁定通知**:
```json
{
  "type": "WORKFLOW_LOCKED",
  "workflowId": 1,
  "reason": "Agent editing"
}
```

**工作流解锁通知**:
```json
{
  "type": "WORKFLOW_UNLOCKED",
  "workflowId": 1
}
```

## 7. 通用响应格式

### 7.1 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 7.2 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "data": null
}
```

### 7.3 常见状态码
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未认证
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误
