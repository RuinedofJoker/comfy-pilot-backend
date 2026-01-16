# Step4: ComfyUI服务模块实现

> 本文档记录ComfyUI服务模块(cfsvr)的完整实现过程

## 📋 目录

- [一、Step3 完成情况](#一step3-完成情况)
- [二、Step4 目标](#二step4-目标)
- [三、数据模型设计](#三数据模型设计)
- [四、领域层实现](#四领域层实现)
- [五、基础设施层实现](#五基础设施层实现)
- [六、应用层实现](#六应用层实现)
- [七、接口层实现](#七接口层实现)
- [八、数据库迁移](#八数据库迁移)

---

## 一、Step3 完成情况

### 1.1 已完成功能

从 Step3 完成的任务：

1. **资源模块完整实现** ✅
   - 文件上传（单个/批量）
   - 文件下载
   - 文件删除
   - 文件列表查询
   - 本地文件存储

2. **通知模块完整实现** ✅
   - 邮件发送服务
   - 邮件日志记录
   - 异步邮件发送
   - 密码重置邮件模板

3. **认证模块完善** ✅
   - 密码重置功能集成邮件服务
   - 完整的密码重置流程

---

## 二、Step4 目标

### 2.1 核心目标

**实现ComfyUI服务模块的核心功能**

### 2.2 功能范围

**ComfyUI服务模块核心功能**：
- 手动创建ComfyUI服务（管理员）
- 代码注册ComfyUI服务（开发者）
- 服务信息查询（列表/详情）
- 服务信息更新（权限控制）
- 服务删除（权限控制）
- 服务连接测试
- 服务健康检查

### 2.3 设计要点

#### 2.3.1 服务注册来源

**两种注册方式**：
1. **手动创建（MANUAL）**：管理员通过管理页面创建
2. **代码注册（CODE_BASED）**：开发者通过代码方式注册

#### 2.3.2 唯一标识符（server_key）

**设计规则**：
- 全局唯一，用于定位ComfyUI服务
- **手动创建**：
  - 管理员可以指定 `server_key`
  - 如果不指定，系统自动生成UUID
  - 不能与已有的重复
- **代码注册**：
  - 开发者必须手动指定 `server_key`
  - 支持幂等性（重复注册更新基本信息）

#### 2.3.3 权限控制策略

根据 `source_type` 字段控制可编辑范围：

| 字段 | 手动创建 | 代码注册 |
|------|---------|---------|
| server_key | ❌ 创建后不可编辑 | ❌ 不可编辑 |
| server_name | ✅ 可编辑 | ✅ 可编辑 |
| description | ✅ 可编辑 | ✅ 可编辑 |
| base_url | ✅ 可编辑 | ❌ 不可编辑 |
| auth_mode | ✅ 可编辑 | ❌ 不可编辑 |
| api_key | ✅ 可编辑 | ❌ 不可编辑 |
| timeout_seconds | ✅ 可编辑 | ❌ 不可编辑 |
| max_retries | ✅ 可编辑 | ❌ 不可编辑 |
| is_enabled | ✅ 可编辑 | ❌ 不可编辑 |

#### 2.3.4 认证模式设计

**auth_mode 字段**：
- 类型：VARCHAR(20)
- 默认值：NULL（无认证）
- 当前支持：NULL（无认证）
- 预留扩展：BASIC_AUTH、OAUTH2、API_KEY 等

**api_key 字段**：
- 保留字段，用于未来认证扩展
- 当前可以存储，但不强制使用

---

## 三、数据模型设计

### 3.1 comfyui_server 表结构

**表名**: `comfyui_server`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键ID（雪花算法） |
| server_key | VARCHAR(100) | NOT NULL UNIQUE | 服务唯一标识符 |
| server_name | VARCHAR(100) | NOT NULL | 服务名称 |
| description | VARCHAR(500) | | 服务描述 |
| base_url | VARCHAR(255) | NOT NULL | ComfyUI服务地址 |
| auth_mode | VARCHAR(20) | | 认证模式（NULL/BASIC_AUTH/OAUTH2等） |
| api_key | VARCHAR(255) | | API密钥（预留字段） |
| timeout_seconds | INT | DEFAULT 30 | 请求超时时间（秒） |
| max_retries | INT | DEFAULT 3 | 最大重试次数 |
| source_type | VARCHAR(20) | NOT NULL | 注册来源：MANUAL/CODE_BASED |
| is_enabled | BOOLEAN | DEFAULT TRUE | 是否启用 |
| last_health_check_time | TIMESTAMP | | 最后健康检查时间 |
| health_status | VARCHAR(20) | | 健康状态：HEALTHY/UNHEALTHY/UNKNOWN |
| create_time | TIMESTAMP | NOT NULL | 创建时间 |
| create_by | BIGINT | NOT NULL | 创建人ID |
| update_time | TIMESTAMP | NOT NULL | 更新时间 |
| update_by | BIGINT | NOT NULL | 更新人ID |
| is_deleted | BIGINT | DEFAULT 0 | 逻辑删除标记 |

**索引设计**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `uk_server_key` (server_key)
- INDEX: `idx_source_type` (source_type)
- INDEX: `idx_is_enabled` (is_enabled)
- INDEX: `idx_health_status` (health_status)

---

## 四、领域层实现

### 4.1 枚举定义

#### 4.1.1 ServerSourceType - 服务注册来源

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/domain/enums/ServerSourceType.java`

**枚举值**:
- MANUAL - 手动创建
- CODE_BASED - 代码注册

#### 4.1.2 HealthStatus - 健康状态

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/domain/enums/HealthStatus.java`

**枚举值**:
- HEALTHY - 健康
- UNHEALTHY - 不健康
- UNKNOWN - 未知

### 4.2 领域实体

#### 4.2.1 ComfyuiServer - ComfyUI服务实体

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/domain/entity/ComfyuiServer.java`

**核心字段**:
- id - 服务ID
- serverKey - 服务唯一标识符
- serverName - 服务名称
- description - 服务描述
- baseUrl - ComfyUI服务地址
- authMode - 认证模式
- apiKey - API密钥
- timeoutSeconds - 请求超时时间
- maxRetries - 最大重试次数
- sourceType - 注册来源
- isEnabled - 是否启用
- lastHealthCheckTime - 最后健康检查时间
- healthStatus - 健康状态

**核心方法**:
- `canModifyConnectionConfig()` - 判断是否允许修改连接配置
- `updateBasicInfo(String serverName, String description)` - 更新基本信息
- `updateConnectionConfig(...)` - 更新连接配置（权限控制）
- `setEnabled(Boolean enabled)` - 启用/禁用服务（权限控制）
- `updateHealthStatus(HealthStatus status)` - 更新健康状态

### 4.3 仓储接口

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/domain/repository/ComfyuiServerRepository.java`

**核心方法**:
- `findById(Long id)` - 根据ID查询
- `findByServerKey(String serverKey)` - 根据serverKey查询
- `findAll()` - 查询所有服务
- `findBySourceType(ServerSourceType sourceType)` - 根据来源类型查询
- `findByIsEnabled(Boolean isEnabled)` - 根据启用状态查询
- `save(ComfyuiServer server)` - 保存服务
- `deleteById(Long id)` - 删除服务

---

## 五、基础设施层实现

### 5.1 持久化对象

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/infrastructure/persistence/po/ComfyuiServerPO.java`

### 5.2 Mapper接口

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/infrastructure/persistence/mapper/ComfyuiServerMapper.java`

### 5.3 转换器

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/infrastructure/persistence/converter/ComfyuiServerConverter.java`

### 5.4 仓储实现

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/infrastructure/persistence/repository/ComfyuiServerRepositoryImpl.java`

---

## 六、应用层实现

### 6.1 DTO定义

#### 6.1.1 ComfyuiServerDTO - 服务信息DTO

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/dto/ComfyuiServerDTO.java`

#### 6.1.2 CreateServerRequest - 创建服务请求

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/dto/CreateServerRequest.java`

**字段**:
- serverKey - 服务唯一标识符（可选，不填则自动生成UUID）
- serverName - 服务名称（必填）
- description - 服务描述
- baseUrl - ComfyUI服务地址（必填）
- authMode - 认证模式
- apiKey - API密钥
- timeoutSeconds - 请求超时时间
- maxRetries - 最大重试次数

#### 6.1.3 UpdateServerRequest - 更新服务请求

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/dto/UpdateServerRequest.java`

#### 6.1.4 RegisterServerByCodeRequest - 代码注册请求

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/dto/RegisterServerByCodeRequest.java`

### 6.2 服务接口

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/service/ComfyuiServerService.java`

**核心方法**:
- `createManually(CreateServerRequest request)` - 手动创建服务
- `registerByCode(RegisterServerByCodeRequest request)` - 代码注册服务
- `updateServer(Long id, UpdateServerRequest request)` - 更新服务
- `deleteServer(Long id)` - 删除服务
- `getById(Long id)` - 根据ID查询
- `getByServerKey(String serverKey)` - 根据serverKey查询
- `listServers(ServerSourceType sourceType, Boolean isEnabled)` - 查询服务列表
- `testConnection(Long id)` - 测试连接
- `performHealthCheck()` - 批量健康检查

### 6.3 服务实现

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/service/impl/ComfyuiServerServiceImpl.java`

---

## 七、接口层实现

### 7.1 Controller

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/interfaces/controller/ComfyuiServerController.java`

**API端点**:
- `POST /api/v1/comfyui-servers` - 创建服务（手动）
- `POST /api/v1/comfyui-servers/register` - 注册服务（代码）
- `GET /api/v1/comfyui-servers` - 查询服务列表
- `GET /api/v1/comfyui-servers/{id}` - 查询服务详情
- `GET /api/v1/comfyui-servers/key/{serverKey}` - 根据serverKey查询
- `PUT /api/v1/comfyui-servers/{id}` - 更新服务
- `DELETE /api/v1/comfyui-servers/{id}` - 删除服务
- `POST /api/v1/comfyui-servers/{id}/test` - 测试连接

### 7.2 DTO转换器

**文件**: `src/main/java/org/joker/comfypilot/cfsvr/application/converter/ComfyuiServerDTOConverter.java`

---

## 八、数据库迁移

### 8.1 创建迁移脚本

**文件**: `src/main/resources/db/migration/V5__create_comfyui_server_table.sql`

---

## 当前进度

- [ ] 数据模型设计
- [ ] 领域层实现
- [ ] 基础设施层实现
- [ ] 应用层实现
- [ ] 接口层实现
- [ ] 数据库迁移
- [ ] 测试验证
