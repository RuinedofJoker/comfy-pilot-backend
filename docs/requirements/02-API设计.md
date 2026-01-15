# API 接口设计

## 通用规范

### 请求格式
- **Base URL**: `/api/v1`
- **Content-Type**: `application/json` (文件上传除外)
- **认证**: `Authorization: Bearer {token}`

### 响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未认证
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误

## 1. 认证接口

### 1.1 用户登录
```
POST /auth/login
Request: { username, password }
Response: { token, user: { id, username, displayName, role } }
```

### 1.2 用户登出
```
POST /auth/logout
Headers: Authorization
Response: { code, message }
```

### 1.3 刷新Token
```
POST /auth/refresh
Headers: Authorization
Response: { token }
```

## 2. 用户管理接口

### 2.1 获取当前用户信息
```
GET /users/me
Response: { id, username, displayName, email, avatarUrl, role, workflowCount, sessionCount }
```

### 2.2 更新用户信息
```
PUT /users/me
Request: { displayName, email, avatarUrl }
Response: { success }
```

### 2.3 修改密码
```
PUT /users/me/password
Request: { oldPassword, newPassword }
Response: { success }
```

## 3. ComfyUI 服务管理接口

### 3.1 获取服务列表
```
GET /services
Response: [{ id, name, url, description, status, workflowCount, sessionCount, lastCheckTime }]
```

### 3.2 创建服务 (管理员)
```
POST /services
Request: { name, url, description }
Response: { id }
```

### 3.3 更新服务 (管理员)
```
PUT /services/{id}
Request: { name, url, description }
Response: { success }
```

### 3.4 删除服务 (管理员)
```
DELETE /services/{id}
Response: { success }
```

### 3.5 测试服务连接 (管理员)
```
POST /services/{id}/test
Response: { status, responseTime }
```

## 4. 工作流管理接口

### 4.1 获取工作流列表
```
GET /workflows
Query: serviceId, favorite, search, sortBy, sortOrder
Response: [{ id, name, description, serviceId, serviceName, isFavorite, sessionCount, versionCount, lastUsedAt }]
```

### 4.2 创建工作流
```
POST /workflows
Request: { name, description, serviceId, workflowData }
Response: { id }
```

### 4.3 更新工作流
```
PUT /workflows/{id}
Request: { name, description, workflowData }
Response: { success }
```

### 4.4 删除工作流
```
DELETE /workflows/{id}
Response: { success }
```

### 4.5 切换收藏状态
```
POST /workflows/{id}/favorite
Request: { favorite }
Response: { success }
```

## 5. 会话管理接口

### 5.1 获取会话列表
```
GET /sessions
Query: workflowId
Response: [{ id, title, workflowId, status, lastMessageSummary, updatedAt }]
```

### 5.2 创建会话
```
POST /sessions
Request: { workflowId, title }
Response: { id }
```

### 5.3 获取会话消息
```
GET /sessions/{id}/messages
Response: [{ id, role, content, createdAt }]
```

### 5.4 发送消息
```
POST /sessions/{id}/messages
Request: { content }
Response: { id, role, content, createdAt }
```

## 6. Agent 管理接口 (管理员)

### 6.1 创建 Agent 配置
```
POST /agents
Request: { name, type, description, modelId, systemPrompt, temperature, maxTokens, toolsConfig }
Response: { id, name, type, status }
```

### 6.2 获取 Agent 列表
```
GET /agents
Query: type, status
Response: [{ id, name, type, modelId, modelName, status, createdAt }]
```

### 6.3 更新 Agent 配置
```
PUT /agents/{id}
Request: { name, systemPrompt, temperature, maxTokens, toolsConfig }
Response: { success }
```

### 6.4 获取 Agent 执行历史
```
GET /agents/{id}/executions
Query: sessionId, status, startTime, endTime
Response: [{ id, agentId, sessionId, executionType, status, durationMs, tokenUsage, startTime }]
```

## 7. AI 模型管理接口 (管理员)

### 7.1 创建模型提供商
```
POST /model-providers
Request: { name, code, baseUrl, apiKey, rateLimit, timeoutMs }
Response: { id, name, code, status }
```

### 7.2 获取模型提供商列表
```
GET /model-providers
Response: [{ id, name, code, status, modelCount }]
```

### 7.3 创建 AI 模型
```
POST /models
Request: { providerId, name, modelCode, modelType, maxTokens, supportStream, supportTools, inputPrice, outputPrice }
Response: { id, name, modelCode, modelType, status }
```

### 7.4 获取模型列表
```
GET /models
Query: providerId, modelType, status
Response: [{ id, name, modelCode, modelType, providerId, providerName, status }]
```

## 8. 文件管理接口

### 8.1 上传头像
```
POST /files/avatar
Content-Type: multipart/form-data
Request: file (图片文件)
Response: { fileId, accessUrl }
```

### 8.2 上传工作流文件
```
POST /files/workflow
Content-Type: multipart/form-data
Request: file (JSON文件), workflowId
Response: { fileId, fileName, fileSize }
```

### 8.3 获取文件列表
```
GET /files
Query: businessType, fileType
Response: [{ id, fileName, fileSize, fileType, accessUrl, createTime }]
```

## 9. WebSocket 接口

### 9.1 连接地址
```
ws://host:port/ws/chat?token={jwt_token}
```

### 9.2 消息格式

**客户端发送**:
```json
{ "type": "SEND_MESSAGE", "sessionId": 1, "content": "string" }
```

**服务端推送**:
```json
{ "type": "MESSAGE_RECEIVED", "sessionId": 1, "message": { id, role, content, createdAt } }
```

**工作流锁定通知**:
```json
{ "type": "WORKFLOW_LOCKED", "workflowId": 1, "reason": "Agent editing" }
```

**工作流解锁通知**:
```json
{ "type": "WORKFLOW_UNLOCKED", "workflowId": 1 }
```

