# Step4: ComfyUI服务模块实现

> 本文档记录ComfyUI服务模块(cfsvr)的完整实现过程

## 📋 目录

- [一、Step3 完成情况](#一step3-完成情况)
- [二、Step4 目标](#二step4-目标)
- [三、核心设计](#三核心设计)
- [四、已完成功能](#四已完成功能)
- [五、实现文件清单](#五实现文件清单)
- [六、问题修复记录](#六问题修复记录)
- [七、下一步计划](#七下一步计划)

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
- ✅ 手动创建ComfyUI服务（管理员）
- ⏳ 代码注册ComfyUI服务（开发者）
- ✅ 服务信息查询（列表/详情）
- ✅ 服务信息更新（权限控制）
- ✅ 服务删除（权限控制）
- ⏳ 服务连接测试
- ⏳ 服务健康检查

---

## 三、核心设计

### 3.1 服务注册来源

**两种注册方式**：
1. **手动创建（MANUAL）**：管理员通过管理页面创建
2. **代码注册（CODE_BASED）**：开发者通过代码方式注册

### 3.2 唯一标识符（server_key）

**设计规则**：
- 全局唯一，用于定位ComfyUI服务
- **手动创建**：
  - 管理员可以指定 `server_key`
  - 如果不指定，系统自动生成UUID
  - 不能与已有的重复
- **代码注册**：
  - 开发者必须手动指定 `server_key`
  - 支持幂等性（重复注册更新基本信息）

### 3.3 权限控制策略

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

### 3.4 数据库表结构

**表名**: `comfyui_server`

**核心字段**:
- `id` - 主键ID（雪花算法）
- `server_key` - 服务唯一标识符（UNIQUE）
- `server_name` - 服务名称
- `description` - 服务描述
- `base_url` - ComfyUI服务地址
- `auth_mode` - 认证模式（NULL/BASIC_AUTH/OAUTH2等）
- `api_key` - API密钥（预留字段）
- `timeout_seconds` - 请求超时时间（秒，默认30）
- `max_retries` - 最大重试次数（默认3）
- `source_type` - 注册来源（MANUAL/CODE_BASED）
- `is_enabled` - 是否启用（默认TRUE）
- `last_health_check_time` - 最后健康检查时间
- `health_status` - 健康状态（HEALTHY/UNHEALTHY/UNKNOWN）

**索引**:
- PRIMARY KEY: `id`
- UNIQUE INDEX: `uk_server_key` (server_key)
- INDEX: `idx_source_type` (source_type)
- INDEX: `idx_is_enabled` (is_enabled)
- INDEX: `idx_health_status` (health_status)

---

## 四、已完成功能

### 4.1 手动创建ComfyUI服务 ✅

**完成时间**: 2026-01-16

**实现内容**:
1. ✅ 完整的DDD四层架构实现
2. ✅ serverKey支持管理员指定或自动生成UUID
3. ✅ serverKey唯一性校验
4. ✅ 权限控制（基于source_type的领域层验证）
5. ✅ 完整的CRUD操作
6. ✅ 数据库迁移脚本

**核心特性**:
- **serverKey可选**：不指定则自动生成UUID
- **重复检查**：指定serverKey时检查是否已存在
- **权限控制**：MANUAL类型可修改所有字段，CODE_BASED类型仅可修改基本信息
- **删除保护**：CODE_BASED类型服务不允许通过管理页面删除

**API端点**:
- `POST /api/v1/comfyui-servers` - 创建服务
- `GET /api/v1/comfyui-servers` - 查询服务列表
- `GET /api/v1/comfyui-servers/{id}` - 查询服务详情
- `GET /api/v1/comfyui-servers/key/{serverKey}` - 根据serverKey查询
- `PUT /api/v1/comfyui-servers/{id}` - 更新服务
- `DELETE /api/v1/comfyui-servers/{id}` - 删除服务

---

## 五、实现文件清单

### 5.1 领域层 (Domain Layer)

**枚举类**:
- `ServerSourceType.java` - 服务注册来源枚举（MANUAL/CODE_BASED）
- `HealthStatus.java` - 健康状态枚举（HEALTHY/UNHEALTHY/UNKNOWN）

**实体类**:
- `ComfyuiServer.java` - ComfyUI服务领域实体
  - 核心方法：`canModifyConnectionConfig()`, `updateBasicInfo()`, `updateConnectionConfig()`, `setEnabled()`, `updateHealthStatus()`

**仓储接口**:
- `ComfyuiServerRepository.java` - 仓储接口定义

### 5.2 基础设施层 (Infrastructure Layer)

**持久化对象**:
- `ComfyuiServerPO.java` - 持久化对象

**Mapper接口**:
- `ComfyuiServerMapper.java` - MyBatis-Plus Mapper

**转换器**:
- `ComfyuiServerConverter.java` - PO ↔ Entity 转换器（MapStruct）

**仓储实现**:
- `ComfyuiServerRepositoryImpl.java` - 仓储接口实现

### 5.3 应用层 (Application Layer)

**DTO类**:
- `ComfyuiServerDTO.java` - 服务信息DTO
- `CreateServerRequest.java` - 创建服务请求DTO
- `UpdateServerRequest.java` - 更新服务请求DTO

**转换器**:
- `ComfyuiServerDTOConverter.java` - Entity ↔ DTO 转换器（MapStruct）

**服务接口**:
- `ComfyuiServerService.java` - 应用服务接口

**服务实现**:
- `ComfyuiServerServiceImpl.java` - 应用服务实现
  - 核心方法：`createManually()`, `updateServer()`, `deleteServer()`, `getById()`, `getByServerKey()`, `listServers()`

### 5.4 接口层 (Interfaces Layer)

**Controller**:
- `ComfyuiServerController.java` - REST API控制器

### 5.5 数据库迁移

**迁移脚本**:
- `V5__create_comfyui_server_table.sql` - 创建comfyui_server表

---

## 六、问题修复记录

### 6.1 认证拦截器逻辑错误修复 ✅

**问题描述**:
- 文件：`AuthInterceptor.java:46`
- 错误：Token验证失败时返回`true`（放行），应该返回`false`（拦截）

**修复内容**:
所有认证失败场景改为返回`false`并设置401响应：
- Token验证失败 → 拦截并返回401
- Token不存在于Redis → 拦截并返回401
- Token已被撤销 → 拦截并返回401
- 用户会话不存在 → 拦截并返回401
- 认证异常 → 拦截并返回500

**修复时间**: 2026-01-16

---

## 七、下一步计划

### 7.1 待实现功能

1. [ ] **代码注册ComfyUI服务功能**
   - RegisterServerByCodeRequest DTO
   - registerByCode() 服务方法
   - 幂等性处理逻辑
   - 启动时自动注册

2. [ ] **服务连接测试功能**
   - testConnection() 方法
   - HTTP连接测试
   - 超时和重试处理

3. [ ] **服务健康检查功能**
   - performHealthCheck() 方法
   - 定时任务调度
   - 健康状态更新

4. [ ] **完整的集成测试**
   - 单元测试
   - 集成测试
   - API测试

---

## 八、当前进度

- [x] 数据模型设计 ✅
- [x] 领域层实现 ✅
- [x] 基础设施层实现 ✅
- [x] 应用层实现 ✅（手动创建功能）
- [x] 接口层实现 ✅（手动创建功能）
- [x] 数据库迁移 ✅
- [ ] 代码注册功能实现
- [ ] 服务连接测试
- [ ] 服务健康检查
- [ ] 测试验证

---

**Step4 状态**: 进行中（手动创建功能已完成，代码注册功能待实现）

**最后更新**: 2026-01-16
