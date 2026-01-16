# Step5: 工作流模块实现

> 本文档记录工作流模块(workflow)的完整实现过程

## 📋 目录

- [一、Step4 完成情况](#一step4-完成情况)
- [二、Step5 目标](#二step5-目标)
- [三、核心设计](#三核心设计)
- [四、已完成功能](#四已完成功能)
- [五、实现文件清单](#五实现文件清单)
- [六、待完成功能](#六待完成功能)
- [七、下一步计划](#七下一步计划)

---

## 一、Step4 完成情况

### 1.1 已完成功能

从 Step4 完成的任务：

1. **ComfyUI服务模块完整实现** ✅
   - 手动创建ComfyUI服务
   - 服务信息查询（列表/详情）
   - 服务信息更新（权限控制）
   - 服务删除（权限控制）
   - 完整的DDD四层架构

2. **认证拦截器逻辑修复** ✅
   - 修复Token验证失败时的返回值错误
   - 所有认证失败场景正确拦截并返回401

---

## 二、Step5 目标

### 2.1 核心目标

**实现工作流模块的核心功能**

### 2.2 功能范围

**工作流模块核心功能**：
- ✅ 工作流CRUD（创建、查询、更新、删除）
- ✅ 工作流内容管理（保存、获取）
- ✅ 工作流锁定控制（锁定、解锁）
- ✅ 工作流版本管理（创建版本、查询版本）
- ⏳ Service实现类（待完成）
- ⏳ Controller实现（待完成）

---

## 三、核心设计

### 3.1 工作流与ComfyUI服务的关系

**关联关系**：
- 一个ComfyUI服务对应多个工作流（一对多）
- 每个工作流必须关联一个ComfyUI服务
- 工作流记录服务的ID和唯一标识符（server_id + server_key）

### 3.2 激活内容机制

**激活内容（active_content）**：
- 存储当前工作流的最新内容
- 与工作流是一对一关系，直接存储在workflow表中
- 初始状态为空（NULL）

**保存机制**：
- 用户点击保存按钮或按Ctrl+S触发保存
- 保存时更新active_content字段
- 同时计算并更新active_content_hash（SHA-256）

### 3.3 版本管理机制

**版本创建时机**：
- 仅在Agent对话修改工作流内容时创建版本
- 用户手动保存（Ctrl+S）不创建版本，只更新active_content

**版本生成流程**：
1. Agent对话完成后，计算修改后内容的SHA-256哈希值
2. 查询是否存在相同哈希值的版本
3. 如果哈希值相同，进一步比对完整内容
4. 如果内容完全一致，复用已有版本（不创建新版本）
5. 如果内容不同，创建新版本（version_number自动递增）

**版本去重策略**：
- 使用content_hash快速判断（O(1)复杂度）
- 哈希值相同时再比对完整内容（确保准确性）
- 避免存储重复的版本内容

**版本只读性**：
- 版本一旦创建就不能修改
- 保证版本历史的完整性和可追溯性
- 版本号严格递增，不允许跳号

### 3.4 工作流锁定机制

**锁定目的**：
- 防止多人同时编辑同一个工作流
- 避免内容冲突和数据丢失

**锁定规则**：
- 用户打开工作流编辑页面时自动锁定
- 锁定时记录locked_by（用户ID）和locked_at（锁定时间）
- 其他用户无法编辑已锁定的工作流

**解锁规则**：
- 用户关闭编辑页面时自动解锁
- 锁定超时自动解锁（如30分钟无操作）
- 锁定人可以主动解锁
- 管理员可以强制解锁

### 3.5 数据库表结构

**workflow表（工作流表）**：
- 主键：id（雪花算法）
- 核心字段：workflow_name, description, comfyui_server_id, comfyui_server_key
- 内容字段：active_content, active_content_hash
- 锁定字段：is_locked, locked_by, locked_at
- 通用字段：create_time, create_by, update_time, update_by, is_deleted

**workflow_version表（工作流版本表）**：
- 主键：id（雪花算法）
- 核心字段：workflow_id, version_number, content, content_hash
- 扩展字段：change_summary, session_id
- 通用字段：create_time, create_by, is_deleted
- 唯一索引：uk_workflow_version (workflow_id, version_number)

---

## 四、已完成功能

### 4.1 需求文档设计 ✅

**完成时间**: 2026-01-16

**实现内容**:
1. ✅ 数据模型文档（`docs/requirements/07-工作流模块/数据模型.md`）
   - workflow表完整设计
   - workflow_version表完整设计
   - 字段说明和索引设计
   - 权限控制规则

2. ✅ 模块设计文档（`docs/requirements/07-工作流模块/模块设计.md`）
   - 模块功能概述
   - 业务逻辑详细说明
   - DDD四层架构设计
   - 核心业务方法定义

3. ✅ API设计文档（`docs/requirements/07-工作流模块/API设计.md`）
   - 12个API接口完整定义
   - 请求响应格式示例
   - 错误响应说明

---

### 4.2 数据库迁移脚本 ✅

**完成时间**: 2026-01-16

**实现内容**:
- ✅ V6__create_workflow_tables.sql
  - workflow表创建语句
  - workflow_version表创建语句
  - 所有索引创建
  - 完整的字段注释

---

### 4.3 领域层实现 ✅

**完成时间**: 2026-01-16

**实现内容**:

1. ✅ **Workflow实体**（`Workflow.java`）
   - 完整的字段定义
   - 内容管理方法：saveContent(), getContent(), calculateContentHash()
   - 锁定控制方法：lock(), unlock(), isLockedBy(), canEdit()
   - SHA-256哈希计算

2. ✅ **WorkflowVersion实体**（`WorkflowVersion.java`）
   - 完整的字段定义
   - 版本比对方法：isSameContent()

3. ✅ **WorkflowRepository接口**（`WorkflowRepository.java`）
   - findById(), findAll()
   - findByComfyuiServerId(), findByIsLocked(), findByCreateBy()
   - save(), deleteById()

4. ✅ **WorkflowVersionRepository接口**（`WorkflowVersionRepository.java`）
   - findById(), findByWorkflowId()
   - findByWorkflowIdAndVersionNumber()
   - findByWorkflowIdAndContentHash()
   - getMaxVersionNumber()
   - save(), deleteById()

---

### 4.4 基础设施层实现 ✅

**完成时间**: 2026-01-16

**实现内容**:

1. ✅ **持久化对象（PO）**
   - WorkflowPO.java - 工作流持久化对象
   - WorkflowVersionPO.java - 版本持久化对象
   - 继承BasePO，使用@SuperBuilder

2. ✅ **Mapper接口**
   - WorkflowMapper.java - 继承BaseMapper
   - WorkflowVersionMapper.java - 继承BaseMapper，包含getMaxVersionNumber()自定义查询

3. ✅ **转换器（Converter）**
   - WorkflowConverter.java - PO ↔ Entity转换（MapStruct）
   - WorkflowVersionConverter.java - PO ↔ Entity转换（MapStruct）

4. ✅ **仓储实现（RepositoryImpl）**
   - WorkflowRepositoryImpl.java - 完整的CRUD实现
   - WorkflowVersionRepositoryImpl.java - 完整的版本管理实现

---

### 4.5 应用层实现 ✅

**完成时间**: 2026-01-16

**实现内容**:

1. ✅ **DTO类**
   - WorkflowDTO.java - 工作流信息DTO
   - WorkflowVersionDTO.java - 版本信息DTO
   - CreateWorkflowRequest.java - 创建工作流请求
   - UpdateWorkflowRequest.java - 更新工作流请求
   - SaveWorkflowContentRequest.java - 保存内容请求
   - CreateVersionRequest.java - 创建版本请求
   - 所有DTO继承BaseDTO，使用@SuperBuilder
   - 完整的Swagger注解

2. ✅ **DTO转换器**
   - WorkflowDTOConverter.java - Entity ↔ DTO转换（MapStruct）
   - WorkflowVersionDTOConverter.java - Entity ↔ DTO转换（MapStruct）

3. ✅ **Service接口**
   - WorkflowService.java - 工作流服务接口
     - createWorkflow(), getById(), listWorkflows()
     - updateWorkflow(), deleteWorkflow()
     - saveContent(), getContent()
     - lockWorkflow(), unlockWorkflow()
   
   - WorkflowVersionService.java - 版本服务接口
     - createVersion(), listVersions(), getVersionById()

---

## 五、实现文件清单

### 5.1 需求文档（docs/requirements/07-工作流模块/）

- ✅ 数据模型.md
- ✅ 模块设计.md
- ✅ API设计.md

### 5.2 数据库迁移（src/main/resources/db/migration/）

- ✅ V6__create_workflow_tables.sql

### 5.3 领域层（domain/）

**实体类（entity/）**:
- ✅ Workflow.java
- ✅ WorkflowVersion.java

**仓储接口（repository/）**:
- ✅ WorkflowRepository.java
- ✅ WorkflowVersionRepository.java

### 5.4 基础设施层（infrastructure/persistence/）

**持久化对象（po/）**:
- ✅ WorkflowPO.java
- ✅ WorkflowVersionPO.java

**Mapper接口（mapper/）**:
- ✅ WorkflowMapper.java
- ✅ WorkflowVersionMapper.java

**转换器（converter/）**:
- ✅ WorkflowConverter.java
- ✅ WorkflowVersionConverter.java

**仓储实现（repository/）**:
- ✅ WorkflowRepositoryImpl.java
- ✅ WorkflowVersionRepositoryImpl.java


### 5.5 应用层（application/）

**DTO类（dto/）**:
- ✅ WorkflowDTO.java
- ✅ WorkflowVersionDTO.java
- ✅ CreateWorkflowRequest.java
- ✅ UpdateWorkflowRequest.java
- ✅ SaveWorkflowContentRequest.java
- ✅ CreateVersionRequest.java

**转换器（converter/）**:
- ✅ WorkflowDTOConverter.java
- ✅ WorkflowVersionDTOConverter.java

**服务接口（service/）**:
- ✅ WorkflowService.java
- ✅ WorkflowVersionService.java

**服务实现（service/impl/）**:
- ⏳ WorkflowServiceImpl.java（待实现）
- ⏳ WorkflowVersionServiceImpl.java（待实现）

### 5.6 接口层（interfaces/）

**控制器（controller/）**:
- ⏳ WorkflowController.java（待实现）
- ⏳ WorkflowVersionController.java（待实现）

---

## 六、待完成功能

### 6.1 应用层Service实现类 ⏳

**WorkflowServiceImpl.java**：
- [ ] createWorkflow() - 创建工作流
- [ ] getById() - 根据ID查询
- [ ] listWorkflows() - 查询列表（支持过滤）
- [ ] updateWorkflow() - 更新工作流信息
- [ ] deleteWorkflow() - 删除工作流
- [ ] saveContent() - 保存工作流内容
- [ ] getContent() - 获取工作流内容
- [ ] lockWorkflow() - 锁定工作流
- [ ] unlockWorkflow() - 解锁工作流

**WorkflowVersionServiceImpl.java**：
- [ ] createVersion() - 创建版本（含去重逻辑）
- [ ] listVersions() - 查询版本列表
- [ ] getVersionById() - 查询版本详情


### 6.2 接口层Controller实现 ⏳

**WorkflowController.java**：
- [ ] POST /api/v1/workflows - 创建工作流
- [ ] GET /api/v1/workflows - 查询工作流列表
- [ ] GET /api/v1/workflows/{id} - 查询工作流详情
- [ ] PUT /api/v1/workflows/{id} - 更新工作流信息
- [ ] DELETE /api/v1/workflows/{id} - 删除工作流
- [ ] POST /api/v1/workflows/{id}/content - 保存工作流内容
- [ ] GET /api/v1/workflows/{id}/content - 获取工作流内容
- [ ] POST /api/v1/workflows/{id}/lock - 锁定工作流
- [ ] POST /api/v1/workflows/{id}/unlock - 解锁工作流

**WorkflowVersionController.java**：
- [ ] POST /api/v1/workflows/{id}/versions - 创建版本
- [ ] GET /api/v1/workflows/{id}/versions - 查询版本列表
- [ ] GET /api/v1/workflows/{workflowId}/versions/{versionId} - 查询版本详情

---

## 七、下一步计划

### 7.1 立即待完成

1. **WorkflowServiceImpl实现**
   - 实现所有业务方法
   - 添加事务管理
   - 添加异常处理
   - 集成雪花算法ID生成

2. **WorkflowVersionServiceImpl实现**
   - 实现版本创建逻辑（含去重）
   - 实现版本查询逻辑
   - 添加事务管理

3. **WorkflowController实现**
   - 实现所有REST API端点
   - 添加完整的Swagger注解
   - 添加参数验证
   - 集成用户上下文

4. **WorkflowVersionController实现**
   - 实现版本管理API端点
   - 添加完整的Swagger注解
   - 添加参数验证

### 7.2 后续优化

1. **单元测试**
   - Service层单元测试
   - Repository层单元测试

2. **集成测试**
   - API集成测试
   - 数据库集成测试

3. **性能优化**
   - 版本查询优化
   - 内容哈希计算优化

---

## 八、当前进度

### 8.1 完成度统计

**总体进度**: 约70%完成

**分层完成度**:
- [x] 需求文档设计 - 100%
- [x] 数据库迁移脚本 - 100%
- [x] 领域层 - 100%
- [x] 基础设施层 - 100%
- [x] 应用层DTO和接口 - 100%
- [ ] 应用层Service实现 - 0%
- [ ] 接口层Controller - 0%

### 8.2 代码统计

**已创建文件数**: 28个

**文件分布**:
- 需求文档: 3个
- 数据库迁移: 1个
- 领域层: 4个
- 基础设施层: 8个
- 应用层: 10个
- 接口层: 0个（待创建2个）

---

**Step5 状态**: 进行中（已完成70%，Service实现和Controller待完成）

**最后更新**: 2026-01-16

