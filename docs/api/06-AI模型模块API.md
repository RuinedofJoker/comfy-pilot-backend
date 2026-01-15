# AI模型模块 API 文档

## 基础信息
- 基础路径: `/api/v1`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer {token}`

---

## 1. 模型提供商管理

### 1.1 创建模型提供商
**接口**: `POST /model-providers`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",              // 提供商名称，必填
  "code": "string",              // 提供商代码，必填
  "baseUrl": "string",           // API基础URL，必填
  "apiKey": "string",            // API密钥，必填
  "rateLimit": 60,               // 速率限制(次/分钟)，可选
  "timeoutMs": 30000,            // 超时时间(毫秒)，可选
  "retryTimes": 3                // 重试次数，可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "OpenAI",
    "code": "OPENAI",
    "baseUrl": "https://api.openai.com/v1",
    "status": "ACTIVE",
    "rateLimit": 60,
    "timeoutMs": 30000,
    "retryTimes": 3,
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 1.2 更新提供商
**接口**: `PUT /model-providers/{id}`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",
  "baseUrl": "string",
  "apiKey": "string",
  "rateLimit": 60,
  "timeoutMs": 30000,
  "retryTimes": 3
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "OpenAI Updated",
    "updateTime": "2026-01-15T11:00:00"
  }
}
```

---

### 1.3 获取提供商列表
**接口**: `GET /model-providers`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "OpenAI",
      "code": "OPENAI",
      "baseUrl": "https://api.openai.com/v1",
      "status": "ACTIVE",
      "rateLimit": 60
    }
  ]
}
```

---

### 1.4 测试提供商连接
**接口**: `POST /model-providers/{id}/test`

**权限**: 管理员

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": true
}
```

---

### 1.5 激活/停用提供商
**接口**: `POST /model-providers/{id}/activate` 或 `POST /model-providers/{id}/deactivate`

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

## 2. AI模型管理

### 2.1 创建AI模型
**接口**: `POST /models`

**权限**: 管理员

**请求体**:
```json
{
  "providerId": 1,               // 提供商ID，必填
  "name": "string",              // 模型名称，必填
  "modelCode": "string",         // 模型代码，必填
  "modelType": "string",         // 模型类型，必填
  "description": "string",       // 描述，可选
  "maxTokens": 4096,             // 最大Token数，必填
  "supportStream": true,         // 是否支持流式，可选
  "supportTools": true,          // 是否支持工具调用，可选
  "inputPrice": 0.001,           // 输入价格(元/1K tokens)，可选
  "outputPrice": 0.002           // 输出价格(元/1K tokens)，可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "providerId": 1,
    "name": "GPT-4",
    "modelCode": "gpt-4",
    "modelType": "LLM",
    "maxTokens": 8192,
    "supportStream": true,
    "supportTools": true,
    "inputPrice": 0.03,
    "outputPrice": 0.06,
    "status": "ACTIVE",
    "createTime": "2026-01-15T10:00:00"
  }
}
```

---

### 2.2 更新模型
**接口**: `PUT /models/{id}`

**权限**: 管理员

**请求体**:
```json
{
  "name": "string",
  "description": "string",
  "maxTokens": 4096,
  "supportStream": true,
  "supportTools": true,
  "inputPrice": 0.001,
  "outputPrice": 0.002
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "GPT-4 Updated",
    "updateTime": "2026-01-15T11:00:00"
  }
}
```

---

### 2.3 获取模型列表
**接口**: `GET /models`

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "providerId": 1,
      "name": "GPT-4",
      "modelCode": "gpt-4",
      "modelType": "LLM",
      "status": "ACTIVE"
    }
  ]
}
```

---

### 2.4 激活/停用模型
**接口**: `POST /models/{id}/activate` 或 `POST /models/{id}/deactivate`

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

## 枚举值说明

### ProviderCode (提供商代码)
- `OPENAI`: OpenAI
- `ANTHROPIC`: Anthropic
- `AZURE_OPENAI`: Azure OpenAI
- `ZHIPU`: 智谱AI
- `QWEN`: 通义千问
- `DEEPSEEK`: DeepSeek

### ProviderStatus (提供商状态)
- `ACTIVE`: 激活
- `INACTIVE`: 未激活
- `ERROR`: 错误

### ModelType (模型类型)
- `LLM`: 大语言模型
- `EMBEDDING`: 向量化模型
- `RERANK`: 重排序模型

### ModelStatus (模型状态)
- `ACTIVE`: 激活
- `INACTIVE`: 未激活
- `DEPRECATED`: 已弃用
