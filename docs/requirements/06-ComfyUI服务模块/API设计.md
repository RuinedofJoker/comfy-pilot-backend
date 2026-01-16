# ComfyUI服务模块 - API设计

> 本文档定义ComfyUI服务模块的API接口

## 基础信息

**Base URL**: `/api/v1/comfyui-servers`

**Controller**: `ComfyuiServerController.java`

**实现状态**: ✅ 手动创建功能已完成，⏳ 代码注册功能待实现

---

## 接口列表

### 1. 创建ComfyUI服务（手动）✅

**接口**: `POST /api/v1/comfyui-servers`

**描述**: 管理员手动创建ComfyUI服务

**请求头**:
```
Authorization: Bearer {access_token}
Content-Type: application/json
```

**请求体** (`CreateServerRequest`):
```json
{
  "serverKey": "my-comfyui-server",  // 可选，不填则自动生成UUID
  "serverName": "本地ComfyUI服务",    // 必填
  "description": "开发环境ComfyUI服务",
  "baseUrl": "http://localhost:8188",  // 必填
  "authMode": null,                    // 可选，默认null
  "apiKey": null,                      // 可选
  "timeoutSeconds": 30,                // 可选，默认30
  "maxRetries": 3                      // 可选，默认3
}
```

**字段验证**:
- `serverName`: 必填，长度2-100字符
- `baseUrl`: 必填，有效URL格式
- `serverKey`: 可选，如指定则检查唯一性

**响应** (`Result<ComfyuiServerDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890,
    "serverKey": "my-comfyui-server",
    "serverName": "本地ComfyUI服务",
    "description": "开发环境ComfyUI服务",
    "baseUrl": "http://localhost:8188",
    "authMode": null,
    "apiKey": null,
    "timeoutSeconds": 30,
    "maxRetries": 3,
    "sourceType": "MANUAL",
    "isEnabled": true,
    "lastHealthCheckTime": null,
    "healthStatus": "UNKNOWN",
    "createTime": "2026-01-16T10:00:00",
    "updateTime": "2026-01-16T10:00:00"
  }
}
```

**错误响应**:
- `400` - 参数验证失败
- `409` - serverKey已存在
- `401` - 未授权
- `500` - 服务器错误

---

### 2. 查询服务列表 ✅

**接口**: `GET /api/v1/comfyui-servers`

**描述**: 查询ComfyUI服务列表，支持按来源类型和启用状态过滤

**请求参数**:
- `sourceType` (可选): 来源类型，枚举值 `MANUAL` 或 `CODE_BASED`
- `isEnabled` (可选): 是否启用，布尔值 `true` 或 `false`

**请求示例**:
```
GET /api/v1/comfyui-servers
GET /api/v1/comfyui-servers?sourceType=MANUAL
GET /api/v1/comfyui-servers?isEnabled=true
GET /api/v1/comfyui-servers?sourceType=MANUAL&isEnabled=true
```

**响应** (`Result<List<ComfyuiServerDTO>>`):
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1234567890,
      "serverKey": "my-comfyui-server",
      "serverName": "本地ComfyUI服务",
      "baseUrl": "http://localhost:8188",
      "sourceType": "MANUAL",
      "isEnabled": true,
      "healthStatus": "UNKNOWN",
      "createTime": "2026-01-16T10:00:00"
    }
  ]
}
```

---

### 3. 根据ID查询服务详情 ✅

**接口**: `GET /api/v1/comfyui-servers/{id}`

**描述**: 根据服务ID查询详细信息

**路径参数**:
- `id`: 服务ID（必填）

**请求示例**:
```
GET /api/v1/comfyui-servers/1234567890
```

**响应** (`Result<ComfyuiServerDTO>`):
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1234567890,
    "serverKey": "my-comfyui-server",
    "serverName": "本地ComfyUI服务",
    "description": "开发环境ComfyUI服务",
    "baseUrl": "http://localhost:8188",
    "sourceType": "MANUAL",
    "isEnabled": true,
    "healthStatus": "UNKNOWN",
    "createTime": "2026-01-16T10:00:00",
    "updateTime": "2026-01-16T10:00:00"
  }
}
```

**错误响应**:
- `404` - 服务不存在
- `500` - 服务器错误

---

### 4. 根据serverKey查询服务 ✅

**接口**: `GET /api/v1/comfyui-servers/key/{serverKey}`

**描述**: 根据服务唯一标识符查询服务信息

**路径参数**:
- `serverKey`: 服务唯一标识符（必填）

**请求示例**:
```
GET /api/v1/comfyui-servers/key/my-comfyui-server
```

**响应**: 同"根据ID查询服务详情"

**错误响应**:
- `404` - 服务不存在
- `500` - 服务器错误

---

### 5. 更新服务信息 ✅

**接口**: `PUT /api/v1/comfyui-servers/{id}`

**描述**: 更新ComfyUI服务信息，权限根据来源类型自动控制

**路径参数**:
- `id`: 服务ID（必填）

**请求体** (`UpdateServerRequest`):
```json
{
  "serverName": "更新后的服务名称",
  "description": "更新后的描述",
  "baseUrl": "http://localhost:8188",
  "authMode": null,
  "apiKey": null,
  "timeoutSeconds": 60,
  "maxRetries": 5,
  "isEnabled": true
}
```

**权限控制**:
- MANUAL类型：所有字段可编辑
- CODE_BASED类型：仅 `serverName` 和 `description` 可编辑

**响应**: 同"创建ComfyUI服务"

**错误响应**:
- `400` - 参数验证失败
- `403` - 权限不足（CODE_BASED类型修改连接配置）
- `404` - 服务不存在
- `500` - 服务器错误

---

### 6. 删除服务 ✅

**接口**: `DELETE /api/v1/comfyui-servers/{id}`

**描述**: 删除ComfyUI服务（CODE_BASED类型不允许删除）

**路径参数**:
- `id`: 服务ID（必填）

**请求示例**:
```
DELETE /api/v1/comfyui-servers/1234567890
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
- `403` - 权限不足（CODE_BASED类型服务不允许删除）
- `404` - 服务不存在
- `500` - 服务器错误

---

**最后更新**: 2026-01-16
