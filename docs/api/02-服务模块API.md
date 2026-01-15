# 服务模块 API 文档

## 基础信息
- 基础路径: `/api/v1/comfyui-services`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer {token}`

---

## 1. 服务管理

### 1.1 创建ComfyUI服务
**接口**: `POST /comfyui-services`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",           // 服务名称，必填
  "url": "string",            // 服务URL，必填
  "description": "string"     // 描述，可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "ComfyUI-Server-1",
    "url": "http://localhost:8188",
    "description": "本地开发服务器",
    "status": "OFFLINE",
    "lastCheckTime": null,
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 更新服务信息
**接口**: `PUT /comfyui-services/{id}`

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
    "id": 1,
    "name": "ComfyUI-Server-1-Updated",
    "url": "http://localhost:8188",
    "description": "更新后的描述",
    "status": "ONLINE",
    "lastCheckTime": "2026-01-15T10:30:00"
  }
}
```

---

### 1.3 获取服务列表
**接口**: `GET /comfyui-services`

**查询参数**:
- `page`: 页码，默认1
- `size`: 每页数量，默认10
- `status`: 状态筛选，可选 (ONLINE/OFFLINE/ERROR)

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
        "name": "ComfyUI-Server-1",
        "url": "http://localhost:8188",
        "description": "本地开发服务器",
        "status": "ONLINE",
        "lastCheckTime": "2026-01-15T10:30:00"
      }
    ]
  }
}
```

---

### 1.4 获取服务详情
**接口**: `GET /comfyui-services/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "ComfyUI-Server-1",
    "url": "http://localhost:8188",
    "description": "本地开发服务器",
    "status": "ONLINE",
    "lastCheckTime": "2026-01-15T10:30:00",
    "createTime": "2026-01-15T10:00:00",
    "updateTime": "2026-01-15T10:30:00"
  }
}
```

---

### 1.5 删除服务
**接口**: `DELETE /comfyui-services/{id}`

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

### 1.6 检查服务状态
**接口**: `POST /comfyui-services/{id}/check`

**权限**: 管理员

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "status": "ONLINE",
    "lastCheckTime": "2026-01-15T10:35:00",
    "responseTime": 150
  }
}
```

---

## 枚举值说明

### ServiceStatus (服务状态)
- `ONLINE`: 在线
- `OFFLINE`: 离线
- `ERROR`: 错误
