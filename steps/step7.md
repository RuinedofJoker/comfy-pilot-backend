# Step7: AI模型模块实现

> 本文档记录 Step7 实现 AI 模型模块的过程

## 📋 目录

- [一、Step7 目标](#一step7-目标)
- [二、需求分析](#二需求分析)
- [三、设计方案](#三设计方案)
- [四、实现内容](#四实现内容)
- [五、实现文件清单](#五实现文件清单)
- [六、当前进度](#六当前进度)
- [七、下一步计划](#七下一步计划)

---

## 一、Step7 目标

### 1.1 核心目标

**实现 AI 模型模块，让平台拥有接入模型的能力**

### 1.2 功能范围

1. **模型接入方式**
   - 远程 API 接入（需要提供商和模型信息）
   - 本地接入（通过代码方式）

2. **模型提供商管理**
   - 提供商 CRUD
   - 提供商类型（OpenAI、Anthropic、阿里云等）

3. **模型配置管理**
   - 模型 CRUD
   - 模型接入方式枚举
   - 每种接入方式的独立逻辑

4. **API 密钥管理**
   - 密钥 CRUD
   - 密钥加密存储

---

## 二、需求分析

### 2.1 模型接入方式

#### 2.1.1 远程 API 接入
- 需要配置模型提供商（Provider）
- 需要配置具体模型（Model）
- 需要 API 密钥
- 通过 HTTP 调用远程 API

**示例**：
- OpenAI GPT-4
- Anthropic Claude
- 阿里云通义千问

#### 2.1.2 本地接入
- 通过代码方式实现
- 不需要提供商信息
- 可能需要本地模型文件路径
- 直接调用本地推理引擎

**示例**：
- Ollama 本地模型
- 自定义本地模型

### 2.2 核心实体

#### 2.2.1 ModelProvider（模型提供商）
- 提供商名称（OpenAI、Anthropic 等）
- 提供商类型（枚举）
- API Base URL
- 是否启用

#### 2.2.2 AiModel（AI 模型）
- 模型名称
- 模型标识符（model_id）
- 接入方式（枚举：REMOTE_API、LOCAL）
- 关联的提供商 ID（远程 API 时必填）
- 模型配置（JSON，存储特定接入方式的配置）
- 是否启用

#### 2.2.3 ModelApiKey（API 密钥）
- 关联的提供商 ID
- API 密钥（加密存储）
- 密钥名称
- 是否启用

### 2.3 接入方式枚举设计

```java
public enum ModelAccessType {
    /**
     * 远程 API 接入
     * 需要提供商信息和 API 密钥
     */
    REMOTE_API("remote_api", "远程API接入"),

    /**
     * 本地接入
     * 通过代码方式实现
     */
    LOCAL("local", "本地接入");

    private final String code;
    private final String description;
}
```

### 2.4 提供商类型枚举设计

```java
public enum ProviderType {
    OPENAI("openai", "OpenAI"),
    ANTHROPIC("anthropic", "Anthropic"),
    ALIYUN("aliyun", "阿里云"),
    CUSTOM("custom", "自定义");

    private final String code;
    private final String name;
}
```

---

## 三、设计方案

### 3.1 数据库表设计

#### 3.1.1 model_provider - 模型提供商表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | PK, AUTO |
| provider_name | VARCHAR(100) | 提供商名称 | NOT NULL |
| provider_type | VARCHAR(50) | 提供商类型 | NOT NULL |
| api_base_url | VARCHAR(500) | API基础URL | |
| description | TEXT | 描述 | |
| is_enabled | BOOLEAN | 是否启用 | DEFAULT TRUE |
| create_time | TIMESTAMP | 创建时间 | NOT NULL |
| update_time | TIMESTAMP | 更新时间 | |
| create_by | BIGINT | 创建人ID | |
| update_by | BIGINT | 更新人ID | |

**索引**：
- `idx_provider_type` - provider_type
- `idx_is_enabled` - is_enabled

#### 3.1.2 ai_model - AI模型表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | PK, AUTO |
| model_name | VARCHAR(100) | 模型名称 | NOT NULL |
| model_identifier | VARCHAR(100) | 模型标识符 | NOT NULL, UNIQUE |
| access_type | VARCHAR(50) | 接入方式 | NOT NULL |
| provider_id | BIGINT | 提供商ID | FK |
| model_config | TEXT | 模型配置(JSON) | |
| description | TEXT | 描述 | |
| is_enabled | BOOLEAN | 是否启用 | DEFAULT TRUE |
| create_time | TIMESTAMP | 创建时间 | NOT NULL |
| update_time | TIMESTAMP | 更新时间 | |
| create_by | BIGINT | 创建人ID | |
| update_by | BIGINT | 更新人ID | |

**索引**：
- `idx_model_identifier` - model_identifier (UNIQUE)
- `idx_access_type` - access_type
- `idx_provider_id` - provider_id
- `idx_is_enabled` - is_enabled

**外键**：
- `fk_ai_model_provider` - provider_id → model_provider(id)

#### 3.1.3 model_api_key - 模型API密钥表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | BIGINT | 主键 | PK, AUTO |
| provider_id | BIGINT | 提供商ID | FK, NOT NULL |
| key_name | VARCHAR(100) | 密钥名称 | NOT NULL |
| api_key | VARCHAR(500) | API密钥(加密) | NOT NULL |
| is_enabled | BOOLEAN | 是否启用 | DEFAULT TRUE |
| create_time | TIMESTAMP | 创建时间 | NOT NULL |
| update_time | TIMESTAMP | 更新时间 | |
| create_by | BIGINT | 创建人ID | |
| update_by | BIGINT | 更新人ID | |

**索引**：
- `idx_provider_id` - provider_id
- `idx_is_enabled` - is_enabled

**外键**：
- `fk_model_api_key_provider` - provider_id → model_provider(id)

### 3.2 DDD 分层设计

```
org.joker.comfypilot.model/
├── domain/                    # 领域层
│   ├── entity/
│   │   ├── ModelProvider.java      # 模型提供商实体
│   │   ├── AiModel.java            # AI模型实体
│   │   └── ModelApiKey.java        # API密钥实体
│   ├── repository/
│   │   ├── ModelProviderRepository.java
│   │   ├── AiModelRepository.java
│   │   └── ModelApiKeyRepository.java
│   └── enums/
│       ├── ModelAccessType.java    # 接入方式枚举
│       └── ProviderType.java       # 提供商类型枚举
├── infrastructure/            # 基础设施层
│   └── persistence/
│       ├── po/
│       │   ├── ModelProviderPO.java
│       │   ├── AiModelPO.java
│       │   └── ModelApiKeyPO.java
│       ├── mapper/
│       │   ├── ModelProviderMapper.java
│       │   ├── AiModelMapper.java
│       │   └── ModelApiKeyMapper.java
│       ├── converter/
│       │   ├── ModelProviderConverter.java
│       │   ├── AiModelConverter.java
│       │   └── ModelApiKeyConverter.java
│       └── repository/
│           ├── ModelProviderRepositoryImpl.java
│           ├── AiModelRepositoryImpl.java
│           └── ModelApiKeyRepositoryImpl.java
├── application/               # 应用层
│   ├── dto/
│   │   ├── ModelProviderDTO.java
│   │   ├── AiModelDTO.java
│   │   ├── ModelApiKeyDTO.java
│   │   ├── CreateProviderRequest.java
│   │   ├── UpdateProviderRequest.java
│   │   ├── CreateModelRequest.java
│   │   ├── UpdateModelRequest.java
│   │   ├── CreateApiKeyRequest.java
│   │   └── UpdateApiKeyRequest.java
│   ├── converter/
│   │   ├── ModelProviderDTOConverter.java
│   │   ├── AiModelDTOConverter.java
│   │   └── ModelApiKeyDTOConverter.java
│   └── service/
│       ├── ModelProviderService.java
│       ├── AiModelService.java
│       ├── ModelApiKeyService.java
│       └── impl/
│           ├── ModelProviderServiceImpl.java
│           ├── AiModelServiceImpl.java
│           └── ModelApiKeyServiceImpl.java
└── interfaces/                # 接口层
    └── controller/
        ├── ModelProviderController.java
        ├── AiModelController.java
        └── ModelApiKeyController.java
```

---

## 四、实现内容

### 4.1 数据库迁移脚本 ⏳

**文件**: `V7__create_model_tables.sql`

**内容**:
- 创建 model_provider 表
- 创建 ai_model 表
- 创建 model_api_key 表
- 创建索引和外键

### 4.2 领域层实现 ⏳

#### 4.2.1 枚举类
- ModelAccessType - 接入方式枚举
- ProviderType - 提供商类型枚举

#### 4.2.2 实体类
- ModelProvider - 模型提供商实体
- AiModel - AI模型实体
- ModelApiKey - API密钥实体

#### 4.2.3 仓储接口
- ModelProviderRepository
- AiModelRepository
- ModelApiKeyRepository

### 4.3 基础设施层实现 ⏳

#### 4.3.1 PO类
- ModelProviderPO
- AiModelPO
- ModelApiKeyPO

#### 4.3.2 Mapper接口
- ModelProviderMapper
- AiModelMapper
- ModelApiKeyMapper

#### 4.3.3 Converter
- ModelProviderConverter
- AiModelConverter
- ModelApiKeyConverter

#### 4.3.4 Repository实现
- ModelProviderRepositoryImpl
- AiModelRepositoryImpl
- ModelApiKeyRepositoryImpl

### 4.4 应用层实现 ⏳

#### 4.4.1 DTO类
- ModelProviderDTO
- AiModelDTO
- ModelApiKeyDTO
- 各种 Request DTO

#### 4.4.2 DTO Converter
- ModelProviderDTOConverter
- AiModelDTOConverter
- ModelApiKeyDTOConverter

#### 4.4.3 Service接口和实现
- ModelProviderService / ModelProviderServiceImpl
- AiModelService / AiModelServiceImpl
- ModelApiKeyService / ModelApiKeyServiceImpl

### 4.5 接口层实现 ⏳

#### 4.5.1 Controller
- ModelProviderController
- AiModelController
- ModelApiKeyController

---

## 五、实现文件清单

### 5.1 数据库迁移（resources/db/migration/）
- ⏳ V7__create_model_tables.sql

### 5.2 领域层（domain/）
- ⏳ ModelAccessType.java - 接入方式枚举
- ⏳ ProviderType.java - 提供商类型枚举
- ⏳ ModelProvider.java - 提供商实体
- ⏳ AiModel.java - 模型实体
- ⏳ ModelApiKey.java - API密钥实体
- ⏳ ModelProviderRepository.java - 提供商仓储接口
- ⏳ AiModelRepository.java - 模型仓储接口
- ⏳ ModelApiKeyRepository.java - 密钥仓储接口

### 5.3 基础设施层（infrastructure/）
- ⏳ ModelProviderPO.java
- ⏳ AiModelPO.java
- ⏳ ModelApiKeyPO.java
- ⏳ ModelProviderMapper.java
- ⏳ AiModelMapper.java
- ⏳ ModelApiKeyMapper.java
- ⏳ ModelProviderConverter.java
- ⏳ AiModelConverter.java
- ⏳ ModelApiKeyConverter.java
- ⏳ ModelProviderRepositoryImpl.java
- ⏳ AiModelRepositoryImpl.java
- ⏳ ModelApiKeyRepositoryImpl.java

### 5.4 应用层（application/）
- ⏳ ModelProviderDTO.java
- ⏳ AiModelDTO.java
- ⏳ ModelApiKeyDTO.java
- ⏳ CreateProviderRequest.java
- ⏳ UpdateProviderRequest.java
- ⏳ CreateModelRequest.java
- ⏳ UpdateModelRequest.java
- ⏳ CreateApiKeyRequest.java
- ⏳ UpdateApiKeyRequest.java
- ⏳ ModelProviderDTOConverter.java
- ⏳ AiModelDTOConverter.java
- ⏳ ModelApiKeyDTOConverter.java
- ⏳ ModelProviderService.java
- ⏳ AiModelService.java
- ⏳ ModelApiKeyService.java
- ⏳ ModelProviderServiceImpl.java
- ⏳ AiModelServiceImpl.java
- ⏳ ModelApiKeyServiceImpl.java

### 5.5 接口层（interfaces/）
- ⏳ ModelProviderController.java
- ⏳ AiModelController.java
- ⏳ ModelApiKeyController.java

**预计总文件数**: 约 38 个

---

## 六、当前进度

### 6.1 完成度统计

**总体进度**: 约 40% 完成 ⏳

**分层完成度**:
- [x] 需求文档设计 - 100% ✅
- [x] 数据库迁移脚本 - 100% ✅
- [x] 领域层 - 100% ✅
- [x] 基础设施层 - 约 75% ⏳（PO、Mapper、Converter、1个Repository完成）
- [ ] 应用层 - 0% ⏳
- [ ] 接口层 - 0% ⏳

### 6.2 已完成文件统计

**已完成文件数**: 15 个

**文件分布**:
- 需求文档: 3 个
- 数据库迁移: 1 个
- 领域层枚举: 2 个
- 领域层实体: 3 个
- 领域层仓储接口: 3 个
- 基础设施层PO: 3 个
- 基础设施层Mapper: 3 个
- 基础设施层Converter: 3 个
- 基础设施层Repository: 1 个（ModelProviderRepositoryImpl）

---

## 七、Step7 总结

### 7.1 完成情况

**已完成**: 约 40% ⏳

Step7 成功完成了以下内容：
- ✅ 需求文档（数据模型、模块设计、API设计）
- ✅ 数据库迁移脚本（3个表 + 索引 + 外键）
- ✅ 领域层（枚举、实体、仓储接口）
- ✅ 基础设施层（PO、Mapper、Converter、1个Repository）

### 7.2 剩余工作

剩余工作已转移到 **Step8**，包括：
- ⏳ 基础设施层剩余（2个Repository实现）
- ⏳ 应用层（DTO、Converter、Service）
- ⏳ 接口层（Controller）

详见：[step8.md](step8.md)

---

**Step7 状态**: ⏳ 部分完成（约40%）

**创建时间**: 2026-01-16
**最后更新**: 2026-01-16
