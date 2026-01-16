# 工作流模块 - API设计

> 本文档定义工作流模块的API接口

## 基础信息

**Base URL**: `/api/v1/workflows`

**Controller**: `WorkflowController.java`, `WorkflowVersionController.java`

**实现状态**: ⏳ 待实现

---

## 工作流管理接口

### 1. 创建工作流 ⏳

**接口**: `POST /api/v1/workflows`

**描述**: 创建新的工作流

**请求头**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**请求体** (`CreateWorkflowRequest`):
```json
{
  "workflowName": "我的工作流",
  "description": "这是一个测试工作流",
  "comfyuiServerId": 1234567890,
  "comfyuiServerKey": "my-comfyui-server"
}
```

**字段验证**:
- `workflowName`: 必填，长度2-100字符
- `comfyuiServerId`: 必填，有效的ComfyUI服务ID
- `comfyuiServerKey`: 必填，有效的ComfyUI服务Key

**响应** (`Result<WorkflowDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 9876543210,
    "workflowName": "我的工作流",
    "description": "这是一个测试工作流",
    "comfyuiServerId": 1234567890,
    "comfyuiServerKey": "my-comfyui-server",
    "activeContent": null,
    "activeContentHash": null,
    "thumbnailUrl": null,
    "isLocked": false,
    "lockedBy": null,
    "lockedAt": null,
    "createTime": "2026-01-16T10:00:00",
    "updateTime": "2026-01-16T10:00:00"
  }
}
```

**错误响应**:
- `400` - 参数验证失败
- `404` - ComfyUI服务不存在
- `401` - 未授权
- `500` - 服务器错误

---

### 2. 查询工作流列表 ⏳

**接口**: `GET /api/v1/workflows`

**描述**: 查询工作流列表，支持按ComfyUI服务和锁定状态过滤

**请求参数**:
- `comfyuiServerId` (可选): ComfyUI服务ID
- `isLocked` (可选): 是否锁定，布尔值 `true` 或 `false`
- `createBy` (可选): 创建人ID

**请求示例**:
```
GET /api/v1/workflows
GET /api/v1/workflows?comfyuiServerId=1234567890
GET /api/v1/workflows?isLocked=true
GET /api/v1/workflows?createBy=100001
```

**响应** (`Result<List<WorkflowDTO>>`):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 9876543210,
      "workflowName": "我的工作流",
      "description": "这是一个测试工作流",
      "comfyuiServerId": 1234567890,
      "comfyuiServerKey": "my-comfyui-server",
      "thumbnailUrl": null,
      "isLocked": false,
      "createTime": "2026-01-16T10:00:00"
    }
  ]
}
```

---

### 3. 根据ID查询工作流详情 ⏳

**接口**: `GET /api/v1/workflows/{id}`

**描述**: 根据工作流ID查询详细信息

**路径参数**:
- `id`: 工作流ID（必填）

**请求示例**:
```
GET /api/v1/workflows/9876543210
```

**响应** (`Result<WorkflowDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 9876543210,
    "workflowName": "我的工作流",
    "description": "这是一个测试工作流",
    "comfyuiServerId": 1234567890,
    "comfyuiServerKey": "my-comfyui-server",
    "activeContent": "{\"nodes\": [...]}",
    "activeContentHash": "abc123...",
    "thumbnailUrl": "https://example.com/thumbnail.png",
    "isLocked": false,
    "lockedBy": null,
    "lockedAt": null,
    "createTime": "2026-01-16T10:00:00",
    "updateTime": "2026-01-16T10:00:00"
  }
}
```

**错误响应**:
- `404` - 工作流不存在
- `500` - 服务器错误

---

### 4. 更新工作流信息 ⏳

**接口**: `PUT /api/v1/workflows/{id}`

**描述**: 更新工作流基本信息（名称、描述）

**路径参数**:
- `id`: 工作流ID（必填）

**请求体** (`UpdateWorkflowRequest`):
```json
{
  "workflowName": "更新后的工作流名称",
  "description": "更新后的描述"
}
```

**响应**: 同"创建工作流"

**错误响应**:
- `400` - 参数验证失败
- `404` - 工作流不存在
- `403` - 工作流已锁定，无法修改
- `500` - 服务器错误

---

### 5. 删除工作流 ⏳

**接口**: `DELETE /api/v1/workflows/{id}`

**描述**: 删除工作流（逻辑删除）

**路径参数**:
- `id`: 工作流ID（必填）

**请求示例**:
```
DELETE /api/v1/workflows/9876543210
```

**响应** (`Result<Void>`):
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误响应**:
- `404` - 工作流不存在
- `403` - 工作流已锁定，无法删除
- `500` - 服务器错误

---


## 工作流内容管理接口

### 6. 保存工作流内容 ⏳

**接口**: `POST /api/v1/workflows/{id}/content`

**描述**: 保存工作流内容（更新active_content）

**路径参数**:
- `id`: 工作流ID（必填）

**请求体** (`SaveWorkflowContentRequest`):
```json
{
  "content": "{\"nodes\": [...], \"links\": [...]}"
}
```

**字段验证**:
- `content`: 必填，有效的JSON字符串

**响应** (`Result<WorkflowDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 9876543210,
    "workflowName": "我的工作流",
    "activeContent": "{\"nodes\": [...]}",
    "activeContentHash": "abc123...",
    "updateTime": "2026-01-16T10:05:00"
  }
}
```

**错误响应**:
- `400` - 参数验证失败（content格式错误）
- `404` - 工作流不存在
- `403` - 工作流已锁定，无法保存
- `500` - 服务器错误

---

### 7. 获取工作流内容 ⏳

**接口**: `GET /api/v1/workflows/{id}/content`

**描述**: 获取工作流的当前激活内容

**路径参数**:
- `id`: 工作流ID（必填）

**请求示例**:
```
GET /api/v1/workflows/9876543210/content
```

**响应** (`Result<String>`):
```json
{
  "code": 200,
  "message": "success",
  "data": "{\"nodes\": [...], \"links\": [...]}"
}
```

**错误响应**:
- `404` - 工作流不存在或内容为空
- `500` - 服务器错误

---


## 工作流锁定控制接口

### 8. 锁定工作流 ⏳

**接口**: `POST /api/v1/workflows/{id}/lock`

**描述**: 锁定工作流（编辑时自动调用）

**路径参数**:
- `id`: 工作流ID（必填）

**请求示例**:
```
POST /api/v1/workflows/9876543210/lock
```

**响应** (`Result<WorkflowDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 9876543210,
    "workflowName": "我的工作流",
    "isLocked": true,
    "lockedBy": 100001,
    "lockedAt": "2026-01-16T10:00:00"
  }
}
```

**错误响应**:
- `404` - 工作流不存在
- `409` - 工作流已被其他用户锁定
- `500` - 服务器错误

---

### 9. 解锁工作流 ⏳

**接口**: `POST /api/v1/workflows/{id}/unlock`

**描述**: 解锁工作流（关闭编辑页面时自动调用）

**路径参数**:
- `id`: 工作流ID（必填）

**请求示例**:
```
POST /api/v1/workflows/9876543210/unlock
```

**响应** (`Result<WorkflowDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 9876543210,
    "workflowName": "我的工作流",
    "isLocked": false,
    "lockedBy": null,
    "lockedAt": null
  }
}
```

**错误响应**:
- `404` - 工作流不存在
- `403` - 无权解锁（非锁定人且非管理员）
- `500` - 服务器错误

---


## 版本管理接口

### 10. 创建工作流版本 ⏳

**接口**: `POST /api/v1/workflows/{id}/versions`

**描述**: 创建工作流版本（Agent对话后调用）

**路径参数**:
- `id`: 工作流ID（必填）

**请求体** (`CreateVersionRequest`):
```json
{
  "content": "{\"nodes\": [...], \"links\": [...]}",
  "changeSummary": "添加了图像处理节点",
  "sessionId": 5555555555
}
```

**字段验证**:
- `content`: 必填，有效的JSON字符串
- `changeSummary`: 可选，变更摘要
- `sessionId`: 可选，关联的会话ID

**响应** (`Result<WorkflowVersionDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 7777777777,
    "workflowId": 9876543210,
    "versionNumber": 1,
    "content": "{\"nodes\": [...]}",
    "contentHash": "abc123...",
    "changeSummary": "添加了图像处理节点",
    "sessionId": 5555555555,
    "createTime": "2026-01-16T10:00:00",
    "createBy": 100001
  }
}
```

**业务逻辑**:
- 自动计算content的SHA-256哈希值
- 查询是否存在相同哈希值的版本
- 如果存在且内容完全一致，返回已有版本
- 如果不存在，创建新版本（version_number自动递增）

**错误响应**:
- `400` - 参数验证失败
- `404` - 工作流不存在
- `500` - 服务器错误

---


### 11. 查询工作流版本列表 ⏳

**接口**: `GET /api/v1/workflows/{id}/versions`

**描述**: 查询指定工作流的所有版本

**路径参数**:
- `id`: 工作流ID（必填）

**请求示例**:
```
GET /api/v1/workflows/9876543210/versions
```

**响应** (`Result<List<WorkflowVersionDTO>>`):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 7777777777,
      "workflowId": 9876543210,
      "versionNumber": 2,
      "contentHash": "def456...",
      "changeSummary": "优化了节点连接",
      "sessionId": 5555555556,
      "createTime": "2026-01-16T11:00:00",
      "createBy": 100001
    },
    {
      "id": 7777777776,
      "workflowId": 9876543210,
      "versionNumber": 1,
      "contentHash": "abc123...",
      "changeSummary": "添加了图像处理节点",
      "sessionId": 5555555555,
      "createTime": "2026-01-16T10:00:00",
      "createBy": 100001
    }
  ]
}
```

**错误响应**:
- `404` - 工作流不存在
- `500` - 服务器错误

---


### 12. 查询版本详情 ⏳

**接口**: `GET /api/v1/workflows/{workflowId}/versions/{versionId}`

**描述**: 查询指定版本的详细信息（包含完整内容）

**路径参数**:
- `workflowId`: 工作流ID（必填）
- `versionId`: 版本ID（必填）

**请求示例**:
```
GET /api/v1/workflows/9876543210/versions/7777777777
```

**响应** (`Result<WorkflowVersionDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 7777777777,
    "workflowId": 9876543210,
    "versionNumber": 1,
    "content": "{\"nodes\": [...], \"links\": [...]}",
    "contentHash": "abc123...",
    "changeSummary": "添加了图像处理节点",
    "sessionId": 5555555555,
    "createTime": "2026-01-16T10:00:00",
    "createBy": 100001
  }
}
```

**错误响应**:
- `404` - 工作流或版本不存在
- `500` - 服务器错误

---

**最后更新**: 2026-01-16
