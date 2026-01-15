# 资源模块 API 文档

## 基础信息
- 基础路径: `/api/v1/files`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer {token}`

---

## 1. 文件管理

### 1.1 上传文件
**接口**: `POST /files`

**请求类型**: `multipart/form-data`

**请求参数**:
- `file`: 文件，必填
- `businessType`: 业务类型，可选 (AVATAR/WORKFLOW_EXPORT/WORKFLOW_IMPORT/TEMP_FILE)
- `businessId`: 业务ID，可选
- `isPublic`: 是否公开，可选，默认false

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "fileName": "avatar.jpg",
    "filePath": "/data/files/avatar/2026-01-15/uuid.jpg",
    "fileSize": 102400,
    "fileType": "IMAGE",
    "mimeType": "image/jpeg",
    "storageType": "LOCAL",
    "businessType": "AVATAR",
    "businessId": null,
    "userId": 1,
    "accessUrl": "http://localhost:8080/api/v1/files/1/download",
    "isPublic": false,
    "expireTime": null,
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 下载文件
**接口**: `GET /files/{id}/download`

**响应**: 文件二进制流

---

### 1.3 获取文件信息
**接口**: `GET /files/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "fileName": "avatar.jpg",
    "filePath": "/data/files/avatar/2026-01-15/uuid.jpg",
    "fileSize": 102400,
    "fileType": "IMAGE",
    "mimeType": "image/jpeg",
    "storageType": "LOCAL",
    "businessType": "AVATAR",
    "accessUrl": "http://localhost:8080/api/v1/files/1/download",
    "isPublic": false,
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.4 获取我的文件列表
**接口**: `GET /files/my`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "fileName": "avatar.jpg",
      "fileSize": 102400,
      "fileType": "IMAGE",
      "businessType": "AVATAR",
      "createTime": "2026-01-15T10:00:00"
    }
  ]
}
```

---

### 1.5 删除文件
**接口**: `DELETE /files/{id}`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 1.6 生成临时访问URL
**接口**: `POST /files/{id}/temporary-url`

**请求参数**:
- `expirationMinutes`: 过期时间(分钟)，默认60

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "http://localhost:8080/api/v1/files/temp/uuid?expires=1234567890&signature=abc123"
}
```

---

## 枚举值说明

### StorageType (存储类型)
- `LOCAL`: 本地存储
- `OSS`: 阿里云OSS
- `S3`: AWS S3

### BusinessType (业务类型)
- `AVATAR`: 头像
- `WORKFLOW_EXPORT`: 工作流导出
- `WORKFLOW_IMPORT`: 工作流导入
- `TEMP_FILE`: 临时文件

### FileType (文件类型)
- `IMAGE`: 图片
- `JSON`: JSON文件
- `ZIP`: ZIP压缩包
- `OTHER`: 其他

---

## 文件验证规则

### 头像上传
- 类型: JPG、PNG、GIF
- 大小: 最大2MB
- 自动压缩: 超过500KB

### 工作流文件
- 类型: JSON
- 大小: 最大10MB
- 格式验证: 有效JSON
