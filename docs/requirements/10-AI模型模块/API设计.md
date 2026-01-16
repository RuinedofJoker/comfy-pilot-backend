# AI模型模块 - API设计

> 本文档定义AI模型模块的API接口

## 一、模型提供商管理 API

### 1.1 创建提供商

**接口**: `POST /api/v1/model-providers`

**请求参数**:
```json
{
  "providerName": "OpenAI",
  "providerType": "openai",
  "apiBaseUrl": "https://api.openai.com/v1",
  "description": "OpenAI官方API"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "providerName": "OpenAI",
    "providerType": "openai",
    "apiBaseUrl": "https://api.openai.com/v1",
    "description": "OpenAI官方API",
    "isEnabled": true,
    "createTime": "2026-01-16T10:00:00"
  }
}
```

### 1.2 查询提供商列表

**接口**: `GET /api/v1/model-providers`

**查询参数**:
- `providerType` (可选): 提供商类型
- `isEnabled` (可选): 是否启用

**响应**: 返回提供商列表

### 1.3 查询提供商详情

**接口**: `GET /api/v1/model-providers/{id}`

**响应**: 返回提供商详细信息

### 1.4 更新提供商

**接口**: `PUT /api/v1/model-providers/{id}`

**请求参数**: 与创建接口相同

### 1.5 删除提供商

**接口**: `DELETE /api/v1/model-providers/{id}`

---

## 二、AI模型管理 API

### 2.1 创建模型

**接口**: `POST /api/v1/ai-models`

**请求参数（远程API接入）**:
```json
{
  "modelName": "GPT-4",
  "modelIdentifier": "gpt-4",
  "accessType": "remote_api",
  "providerId": 1,
  "modelConfig": "{\"max_tokens\": 8192}",
  "description": "GPT-4模型"
}
```

**请求参数（本地接入）**:
```json
{
  "modelName": "Llama2 Local",
  "modelIdentifier": "llama2-local",
  "accessType": "local",
  "modelConfig": "{\"model_path\": \"/models/llama2\"}",
  "description": "本地Llama2模型"
}
```

### 2.2 查询模型列表

**接口**: `GET /api/v1/ai-models`

**查询参数**:
- `accessType` (可选): 接入方式
- `providerId` (可选): 提供商ID
- `isEnabled` (可选): 是否启用

### 2.3 查询模型详情

**接口**: `GET /api/v1/ai-models/{id}`

### 2.4 更新模型

**接口**: `PUT /api/v1/ai-models/{id}`

### 2.5 删除模型

**接口**: `DELETE /api/v1/ai-models/{id}`

---

## 三、API密钥管理 API

### 3.1 创建API密钥

**接口**: `POST /api/v1/model-api-keys`

**请求参数**:
```json
{
  "providerId": 1,
  "keyName": "Production Key",
  "apiKey": "sk-xxx"
}
```

### 3.2 查询密钥列表

**接口**: `GET /api/v1/model-api-keys`

**查询参数**:
- `providerId` (可选): 提供商ID

### 3.3 更新密钥

**接口**: `PUT /api/v1/model-api-keys/{id}`

### 3.4 删除密钥

**接口**: `DELETE /api/v1/model-api-keys/{id}`

---

**文档版本**: v1.0
**最后更新**: 2026-01-16
