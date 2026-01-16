# 资源模块 - API设计

> 本文档定义资源模块的API接口

## 接口列表

### 1. 上传单个文件

**接口**: `POST /api/v1/files/upload`

**请求参数**:
- `file` (MultipartFile, 必填): 上传的文件
- `businessType` (String, 可选): 业务类型
- `businessId` (Long, 可选): 业务ID
- `userId` (Long, 隐藏): 从请求属性获取

**响应**: `Result<FileResourceDTO>`

---

### 2. 批量上传文件

**接口**: `POST /api/v1/files/upload/batch`

**请求参数**:
- `files` (List<MultipartFile>, 必填): 文件列表
- `businessType` (String, 可选): 业务类型
- `businessId` (Long, 可选): 业务ID
- `userId` (Long, 隐藏): 从请求属性获取

**响应**: `Result<List<FileResourceDTO>>`

---

### 3. 下载文件

**接口**: `GET /api/v1/files/download/{fileId}`

**路径参数**:
- `fileId` (Long, 必填): 文件ID

**响应**: `ResponseEntity<InputStreamResource>` (文件流)

---

### 4. 删除文件

**接口**: `DELETE /api/v1/files/{fileId}`

**路径参数**:
- `fileId` (Long, 必填): 文件ID

**请求参数**:
- `userId` (Long, 隐藏): 从请求属性获取

**响应**: `Result<Void>`

---

### 5. 查询用户文件列表

**接口**: `GET /api/v1/files/user`

**请求参数**:
- `userId` (Long, 隐藏): 从请求属性获取

**响应**: `Result<List<FileResourceDTO>>`

---

### 6. 查询业务关联文件

**接口**: `GET /api/v1/files/business`

**请求参数**:
- `businessType` (String, 必填): 业务类型
- `businessId` (Long, 必填): 业务ID

**响应**: `Result<List<FileResourceDTO>>`

---

## DTO定义

### FileResourceDTO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 文件ID |
| fileName | String | 原始文件名 |
| storedName | String | 存储文件名 |
| filePath | String | 文件路径 |
| fileSize | Long | 文件大小(字节) |
| fileType | String | MIME类型 |
| fileExtension | String | 文件扩展名 |
| uploadUserId | Long | 上传用户ID |
| businessType | String | 业务类型 |
| businessId | Long | 业务ID |
| downloadCount | Integer | 下载次数 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

