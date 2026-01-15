# 工作流模块 API 文档

## 基础信息
- 基础路径: `/api/v1/workflows`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer {token}`

---

## 1. 工作流管理

### 1.1 创建工作流
**接口**: `POST /workflows`

**请求体**:
```json
{
  "serviceId": 1,                    // 服务ID，必填
  "name": "string",                  // 工作流名称，必填
  "description": "string",           // 描述，可选
  "workflowData": {}                 // 工作流JSON数据，必填
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
    "serviceId": 1,
    "name": "我的工作流",
    "description": "测试工作流",
    "workflowData": {},
    "isFavorite": false,
    "lastUsedAt": null,
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 更新工作流
**接口**: `PUT /workflows/{id}`

**请求体**:
```json
{
  "name": "string",
  "description": "string",
  "workflowData": {}
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "更新后的工作流",
    "description": "更新后的描述",
    "workflowData": {},
    "updateTime": "2026-01-15T11:00:00"
  }
}
```

---

### 1.3 获取工作流列表
**接口**: `GET /workflows`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10
- `serviceId`: 服务ID筛选，可选
- `isFavorite`: 是否收藏筛选，可选

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 20,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": 1,
        "name": "我的工作流",
        "description": "测试工作流",
        "serviceId": 1,
        "isFavorite": true,
        "lastUsedAt": "2026-01-15T10:30:00",
        "createTime": "2026-01-15T10:00:00"
      }
    ]
  }
}
```

---

### 1.4 获取工作流详情
**接口**: `GET /workflows/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "serviceId": 1,
    "name": "我的工作流",
    "description": "测试工作流",
    "workflowData": {},
    "isFavorite": true,
    "lastUsedAt": "2026-01-15T10:30:00",
    "createTime": "2026-01-15T10:00:00",
    "updateTime": "2026-01-15T11:00:00"
  }
}
```

---

### 1.5 删除工作流
**接口**: `DELETE /workflows/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.6 收藏/取消收藏工作流
**接口**: `POST /workflows/{id}/favorite`

**请求体**:
```json
{
  "isFavorite": true
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 2. 工作流版本管理

### 2.1 获取工作流版本列表
**接口**: `GET /workflows/{workflowId}/versions`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 5,
    "page": 1,
    "size": 10,
    "items": [
      {
        "id": 1,
        "workflowId": 1,
        "sessionId": 1,
        "versionNumber": 1,
        "changeSummary": "初始版本",
        "createTime": "2026-01-15T10:00:00"
      }
    ]
  }
}
```

---

### 2.2 获取版本详情
**接口**: `GET /workflows/{workflowId}/versions/{versionId}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "workflowId": 1,
    "sessionId": 1,
    "versionNumber": 1,
    "workflowData": {},
    "changeSummary": "初始版本",
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 2.3 恢复到指定版本
**接口**: `POST /workflows/{workflowId}/versions/{versionId}/restore`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "workflowData": {},
    "updateTime": "2026-01-15T12:00:00"
  }
}
```
