# Step3: 资源模块和通知模块实现

> 本文档记录资源模块(Resources)和通知模块(Notification)的完整实现过程

## 📋 目录

- [一、Step2 遗留问题](#一step2-遗留问题)
- [二、Step3 目标](#二step3-目标)
- [三、资源模块实现](#三资源模块实现)
- [四、通知模块实现](#四通知模块实现)
- [五、配置文件](#五配置文件)
- [六、数据库迁移](#六数据库迁移)
- [七、测试验证](#七测试验证)

---

## 一、Step2 遗留问题

### 1.1 已完成功能

从 Step2 完成的任务：

1. **权限模块核心实现** ✅
   - 角色管理（Role）
   - 权限定义管理（Permission）
   - 用户角色关联（UserRole）
   - 角色权限关联（RolePermission）

2. **认证模块集成** ✅
   - 登录时加载用户角色和权限
   - 注册时自动分配默认角色
   - Session 中存储真实的权限信息

3. **系统初始化** ✅
   - 应用启动时创建系统内置角色
   - 初始化基础权限定义

4. **数据库问题修复** ✅
   - 修复 MyBatis-Plus 逻辑删除类型不匹配问题
   - 修复关联表 ID 生成策略问题

### 1.2 待完善功能

从 Step2 继承的未完成任务：

1. **密码重置功能** ⏸️
   - 当前状态：基础框架已实现，缺少邮件发送功能
   - Step3 计划：在通知模块实现后完善邮件发送

---

## 二、Step3 目标

### 2.1 核心目标

**实现资源模块和通知模块的核心功能**

### 2.2 功能范围

**资源模块核心功能**：
- 文件上传（支持多文件上传）
- 文件下载
- 文件删除
- 文件列表查询
- 本地文件存储（配置化存储路径）
- 文件元数据管理

**通知模块核心功能**：
- 邮件发送服务
- 邮件发送日志记录
- 邮件发送状态追踪
- SMTP 配置管理
- 异步邮件发送（使用 Spring @Async）

### 2.3 技术要求

**资源模块**：
- 遵循 DDD 四层架构
- 使用本地文件系统存储
- 在 application.yml 配置文件根目录
- 支持文件类型验证和大小限制
- 生成唯一文件名避免冲突
- 记录文件元数据到数据库

**通知模块**：
- 遵循 DDD 三层架构（Domain/Application/Infrastructure）
- **作为内部服务，不对外暴露 Controller 接口**
- 使用 Spring Mail 发送邮件
- 在 application.yml 配置 SMTP 信息
- 异步发送邮件，不阻塞主线程
- 记录发送日志和状态
- 支持 HTML 邮件模板
- 由其他业务模块（如认证模块）通过依赖注入调用

---

## 三、资源模块实现

### 3.1 数据模型设计

#### 3.1.1 file_resource 表结构

**表名**: `file_resource`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键ID（雪花算法） |
| file_name | VARCHAR(255) | NOT NULL | 原始文件名 |
| stored_name | VARCHAR(255) | NOT NULL | 存储文件名（唯一） |
| file_path | VARCHAR(500) | NOT NULL | 文件存储路径 |
| file_size | BIGINT | NOT NULL | 文件大小（字节） |
| file_type | VARCHAR(100) | | 文件MIME类型 |
| file_extension | VARCHAR(50) | | 文件扩展名 |
| upload_user_id | BIGINT | NOT NULL | 上传用户ID |
| business_type | VARCHAR(50) | | 业务类型（workflow/avatar等） |
| business_id | BIGINT | | 业务关联ID |
| download_count | INT | DEFAULT 0 | 下载次数 |
| create_time | TIMESTAMP | NOT NULL | 创建时间 |
| create_by | BIGINT | NOT NULL | 创建人ID |
| update_time | TIMESTAMP | NOT NULL | 更新时间 |
| update_by | BIGINT | NOT NULL | 更新人ID |
| is_deleted | BIGINT | DEFAULT 0 | 逻辑删除标记 |

**索引设计**:
- PRIMARY KEY: `id`
- INDEX: `idx_upload_user_id` (upload_user_id)
- INDEX: `idx_business` (business_type, business_id)
- INDEX: `idx_stored_name` (stored_name) - 唯一索引
- INDEX: `idx_create_time` (create_time)

### 3.2 领域层 (Domain)

#### 3.2.1 创建文件资源实体

**文件**: `src/main/java/org/joker/comfypilot/resource/domain/entity/FileResource.java`

**核心字段**:
- id - 文件资源ID
- fileName - 原始文件名
- storedName - 存储文件名
- filePath - 文件存储路径
- fileSize - 文件大小
- fileType - 文件MIME类型
- fileExtension - 文件扩展名
- uploadUserId - 上传用户ID
- businessType - 业务类型
- businessId - 业务关联ID
- downloadCount - 下载次数

**核心方法**:
- `incrementDownloadCount()` - 增加下载次数
- `updateBusinessInfo()` - 更新业务关联信息
- `getFullPath()` - 获取完整文件路径

#### 3.2.2 创建仓储接口

**文件**: `src/main/java/org/joker/comfypilot/resource/domain/repository/FileResourceRepository.java`

**核心方法**:
- `findById(Long id)` - 根据ID查询文件资源
- `findByStoredName(String storedName)` - 根据存储文件名查询
- `findByUploadUserId(Long userId)` - 查询用户上传的文件列表
- `findByBusinessInfo(String businessType, Long businessId)` - 根据业务信息查询
- `save(FileResource fileResource)` - 保存文件资源
- `deleteById(Long id)` - 删除文件资源

### 3.3 应用层 (Application)

#### 3.3.1 创建文件上传服务

**文件**: `src/main/java/org/joker/comfypilot/resource/application/service/FileUploadService.java`

**核心方法**:
- `uploadFile(MultipartFile file, Long userId, String businessType, Long businessId)` - 上传单个文件
- `uploadFiles(List<MultipartFile> files, Long userId, String businessType, Long businessId)` - 批量上传文件
- `generateStoredName(String originalFilename)` - 生成唯一存储文件名
- `validateFile(MultipartFile file)` - 验证文件（类型、大小）

#### 3.3.2 创建文件下载服务

**文件**: `src/main/java/org/joker/comfypilot/resource/application/service/FileDownloadService.java`

**核心方法**:
- `downloadFile(Long fileId)` - 根据ID下载文件
- `downloadFileByStoredName(String storedName)` - 根据存储名下载文件
- `getFileInputStream(FileResource fileResource)` - 获取文件输入流

#### 3.3.3 创建文件管理服务

**文件**: `src/main/java/org/joker/comfypilot/resource/application/service/FileManagementService.java`

**核心方法**:
- `deleteFile(Long fileId, Long userId)` - 删除文件（物理删除+逻辑删除）
- `listUserFiles(Long userId)` - 查询用户文件列表
- `listBusinessFiles(String businessType, Long businessId)` - 查询业务关联文件

---

## 四、通知模块实现

> **重要架构决策**：通知模块作为内部服务，不对外暴露 REST API 接口。邮件发送功能仅供其他业务模块（如认证模块的密码重置、用户注册等）通过依赖注入调用。

### 4.1 数据模型设计

#### 4.1.1 email_log 表结构

**表名**: `email_log`

**字段定义**:

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT | PRIMARY KEY | 主键ID（雪花算法） |
| recipient | VARCHAR(255) | NOT NULL | 收件人邮箱 |
| subject | VARCHAR(500) | NOT NULL | 邮件主题 |
| content | TEXT | NOT NULL | 邮件内容 |
| send_status | VARCHAR(20) | NOT NULL | 发送状态（PENDING/SUCCESS/FAILED） |
| error_message | TEXT | | 错误信息 |
| send_time | TIMESTAMP | | 实际发送时间 |
| retry_count | INT | DEFAULT 0 | 重试次数 |
| business_type | VARCHAR(50) | | 业务类型（PASSWORD_RESET/REGISTER等） |
| business_id | VARCHAR(100) | | 业务关联ID |
| create_time | TIMESTAMP | NOT NULL | 创建时间 |
| create_by | BIGINT | NOT NULL | 创建人ID |
| update_time | TIMESTAMP | NOT NULL | 更新时间 |
| update_by | BIGINT | NOT NULL | 更新人ID |
| is_deleted | BIGINT | DEFAULT 0 | 逻辑删除标记 |

**索引设计**:
- PRIMARY KEY: `id`
- INDEX: `idx_recipient` (recipient)
- INDEX: `idx_send_status` (send_status)
- INDEX: `idx_business` (business_type, business_id)
- INDEX: `idx_create_time` (create_time)

### 4.2 领域层 (Domain)

#### 4.2.1 创建邮件日志实体

**文件**: `src/main/java/org/joker/comfypilot/notification/domain/entity/EmailLog.java`

**核心字段**:
- id - 邮件日志ID
- recipient - 收件人邮箱
- subject - 邮件主题
- content - 邮件内容
- sendStatus - 发送状态（枚举：PENDING/SUCCESS/FAILED）
- errorMessage - 错误信息
- sendTime - 实际发送时间
- retryCount - 重试次数
- businessType - 业务类型
- businessId - 业务关联ID

**核心方法**:
- `markAsSent()` - 标记为发送成功
- `markAsFailed(String errorMessage)` - 标记为发送失败
- `incrementRetryCount()` - 增加重试次数
- `canRetry()` - 判断是否可以重试

#### 4.2.2 创建邮件发送状态枚举

**文件**: `src/main/java/org/joker/comfypilot/notification/domain/enums/EmailSendStatus.java`

**枚举值**:
- PENDING - 待发送
- SUCCESS - 发送成功
- FAILED - 发送失败

#### 4.2.3 创建仓储接口

**文件**: `src/main/java/org/joker/comfypilot/notification/domain/repository/EmailLogRepository.java`

**核心方法**:
- `findById(Long id)` - 根据ID查询邮件日志
- `findByRecipient(String recipient)` - 查询收件人的邮件日志
- `findByBusinessInfo(String businessType, String businessId)` - 根据业务信息查询
- `save(EmailLog emailLog)` - 保存邮件日志

### 4.3 应用层 (Application)

#### 4.3.1 创建邮件发送服务

**文件**: `src/main/java/org/joker/comfypilot/notification/application/service/EmailService.java`

**核心方法**:
- `sendEmail(String recipient, String subject, String content)` - 发送普通邮件
- `sendHtmlEmail(String recipient, String subject, String htmlContent)` - 发送HTML邮件
- `sendEmailAsync(String recipient, String subject, String content)` - 异步发送邮件
- `sendPasswordResetEmail(String recipient, String resetToken)` - 发送密码重置邮件

**使用示例**:

其他业务模块通过依赖注入使用邮件服务：

```java
@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;  // 注入邮件服务

    public void resetPassword(String email) {
        // 生成重置令牌
        String resetToken = generateResetToken();

        // 调用邮件服务发送密码重置邮件
        emailService.sendPasswordResetEmail(email, resetToken);
    }
}
```

### 4.4 模块结构说明

**最终目录结构**:
```
notification/
├── application/
│   └── service/
│       └── EmailService.java          # 邮件发送服务（供其他模块调用）
├── domain/
│   ├── entity/
│   │   └── EmailLog.java              # 邮件日志实体
│   ├── enums/
│   │   └── EmailSendStatus.java       # 邮件发送状态枚举
│   └── repository/
│       └── EmailLogRepository.java    # 邮件日志仓储接口
└── infrastructure/
    └── persistence/
        ├── converter/
        │   └── EmailLogConverter.java # 实体转换器
        ├── mapper/
        │   └── EmailLogMapper.java    # MyBatis Mapper
        ├── po/
        │   └── EmailLogPO.java        # 持久化对象
        └── repository/
            └── EmailLogRepositoryImpl.java  # 仓储实现
```

**注意**：通知模块不包含 `interfaces` 层，因为它不对外暴露 REST API。

---

## 五、配置文件

### 5.1 application.yml 配置

**文件存储配置**:
```yaml
file:
  storage:
    root-path: ./data/files  # 文件存储根目录
    max-file-size: 10485760  # 最大文件大小（10MB）
    allowed-extensions: jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,txt,json  # 允许的文件扩展名
```

**邮件SMTP配置**:
```yaml
spring:
  mail:
    host: smtp.example.com  # SMTP服务器地址
    port: 587  # SMTP端口
    username: your-email@example.com  # 发送方邮箱
    password: your-password  # 邮箱密码或授权码
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
    default-encoding: UTF-8
```

---

## 六、数据库迁移

### 6.1 创建迁移脚本

**文件**: `src/main/resources/db/migration/V4__create_resource_and_notification_tables.sql`

---

## 七、测试验证

### 7.1 资源模块测试

- 测试文件上传功能
- 测试文件下载功能
- 测试文件删除功能
- 测试文件列表查询

### 7.2 通知模块测试

- 测试邮件发送功能
- 测试异步邮件发送
- 测试邮件日志记录
- 测试密码重置邮件发送

