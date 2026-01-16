# ComfyUI Pilot Backend

## 📋 项目简介

ComfyUI Pilot 是一个 ComfyUI 的增强代理层系统，旨在为 ComfyUI 提供统一管理、AI 辅助编辑和版本管理能力。

### 核心价值

- **统一管理**：集中管理多个 ComfyUI 服务实例
- **AI 辅助**：通过 AI Agent 实现自然语言辅助编辑工作流
- **版本管理**：提供工作流版本管理和会话历史追踪
- **多模型支持**：集成多种 AI 模型提供商（OpenAI/Anthropic/国产模型）

### 系统架构

```
用户界面 (Web + AI Chat)
        ↓
代理系统 (认证/存储/AI/管理)
        ↓
ComfyUI 服务集群
```

## 🚀 技术栈

### 后端框架
- **Spring Boot**: 3.5.9
- **Java**: 21
- **构建工具**: Maven

### 数据存储
- **数据库**: PostgreSQL (支持 PGVector 向量扩展)
- **缓存**: Redis
- **ORM**: MyBatis-Plus 3.5.9

### 核心依赖
- **AI 框架**: LangChain4j 1.3.0
- **JSON 处理**: Fastjson2 2.0.53
- **工具库**: Hutool 5.8.26
- **对象映射**: MapStruct 1.6.3
- **API 文档**: SpringDoc OpenAPI 2.7.0 (Swagger)

## 📁 项目结构

项目采用 **领域驱动设计（DDD）** 架构模式，按业务领域划分模块。详细模块见`docs/requirements`

## 🧩 核心模块说明

### DDD 四层架构

每个业务模块都遵循 DDD 四层架构设计：

#### 1. **接口层 (interfaces)**
- **职责**：对外提供 REST API 接口
- **组件**：Controller、DTO 转换
- **示例**：`UserController.java`

#### 2. **应用层 (application)**
- **职责**：编排业务流程，协调领域对象
- **组件**：ApplicationService、DTO 定义
- **示例**：`UserApplicationService.java`

#### 3. **领域层 (domain)**
- **职责**：核心业务逻辑和规则
- **组件**：Entity、Repository 接口、枚举
- **示例**：`User.java`、`UserRepository.java`

#### 4. **基础设施层 (infrastructure)**
- **职责**：技术实现细节
- **组件**：Repository 实现、Mapper、PO
- **示例**：`UserRepositoryImpl.java`、`UserMapper.java`

### 业务模块详解

#### 1. **用户模块 (user)**
- **功能**：用户认证、权限管理、个人设置
- **核心实体**：User（用户）
- **主要功能**：
  - 用户注册/登录
  - 权限验证
  - 用户信息管理

#### 2. **ComfyUI 服务模块 (comfyui)**
- **功能**：管理多个 ComfyUI 服务实例
- **核心实体**：ComfyuiService（ComfyUI 服务）
- **主要功能**：
  - 服务实例注册
  - 服务状态监控
  - 服务健康检查
  - 负载均衡

#### 3. **工作流模块 (workflow)**
- **功能**：工作流的创建、编辑、版本管理
- **核心实体**：Workflow（工作流）、WorkflowVersion（工作流版本）
- **主要功能**：
  - 工作流 CRUD 操作
  - 工作流版本管理
  - 工作流验证
  - 工作流模板管理

#### 4. **会话模块 (session)**
- **功能**：AI 对话会话管理
- **核心实体**：Session（会话）、Message（消息）
- **主要功能**：
  - 会话创建与管理
  - 消息记录与检索
  - 会话历史追踪
  - 上下文管理

#### 5. **Agent 模块 (agent)**
- **功能**：AI Agent 配置与执行管理
- **核心实体**：AgentConfig（Agent 配置）、AgentExecution（Agent 执行记录）
- **主要功能**：
  - Agent 配置管理
  - Agent 调度执行
  - 执行状态追踪
  - 执行结果记录

#### 6. **AI 模型模块 (model)**
- **功能**：AI 模型提供商和模型配置管理
- **核心实体**：ModelProvider（模型提供商）、ModelConfig（模型配置）
- **主要功能**：
  - 模型提供商管理
  - 模型配置管理
  - 模型调用统计
  - API Key 管理

#### 7. **资源模块 (resource)**
- **功能**：文件上传、存储和管理
- **核心实体**：Resource（资源）
- **主要功能**：
  - 文件上传
  - 文件存储管理
  - 文件访问控制
  - 文件元数据管理

#### 8. **公共模块 (common)**
- **功能**：提供通用基础设施和工具
- **主要组件**：
  - **配置类**：全局异常处理、Redis 配置、Swagger 配置等
  - **基础类**：BaseEntity（实体基类）、BaseDTO（DTO 基类）
  - **工具类**：通用工具方法
  - **常量定义**：响应码、业务常量等

## 🚀 快速开始

### 环境要求

- **JDK**: 21+
- **Maven**: 3.8+
- **PostgreSQL**: 14+ (需安装 PGVector 扩展)
- **Redis**: 6.0+

### 安装步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd comfy-pilot-backend
```

#### 2. 配置数据库

创建 PostgreSQL 数据库并安装 PGVector 扩展：

```sql
CREATE DATABASE comfy_pilot;
\c comfy_pilot
CREATE EXTENSION vector;
```

#### 3. 配置应用

修改 `src/main/resources/application.yml` 配置文件：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/comfy_pilot
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
```

#### 4. 构建项目

```bash
mvn clean install
```

#### 5. 运行应用

```bash
mvn spring-boot:run
```

应用启动后，访问以下地址：

- **应用地址**: http://localhost:8080
- **Swagger 文档**: http://localhost:8080/swagger-ui.html
- **API 文档**: http://localhost:8080/v3/api-docs

## 📖 开发指南

### 新增业务模块

当需要新增一个业务模块时，请遵循以下步骤：

#### 1. 创建模块目录结构

```
src/main/java/org/joker/comfypilot/[module-name]/
├── application/
│   ├── dto/
│   └── service/
├── domain/
│   ├── entity/
│   ├── enums/
│   └── repository/
├── infrastructure/
│   └── persistence/
│       ├── mapper/
│       ├── po/
│       └── repository/
└── interfaces/
    └── controller/
```

#### 2. 编写领域实体

继承 `BaseEntity` 基类，包含通用字段（id、创建时间、更新时间等）：

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class YourEntity extends BaseEntity {
    private String name;
    // 其他业务字段
}
```

#### 3. 定义仓储接口

在 domain 层定义仓储接口：

```java
public interface YourRepository {
    YourEntity save(YourEntity entity);
    Optional<YourEntity> findById(Long id);
    // 其他方法
}
```

#### 4. 实现持久化层

创建 MyBatis Mapper 和仓储实现：

```java
@Mapper
public interface YourMapper extends BaseMapper<YourPO> {
    // 自定义 SQL 方法
}

@Repository
public class YourRepositoryImpl implements YourRepository {
    @Autowired
    private YourMapper mapper;

    // 实现仓储接口方法
}
```

#### 5. 编写应用服务

```java
@Service
public class YourApplicationService {
    @Autowired
    private YourRepository repository;

    // 业务逻辑方法
}
```

#### 6. 创建 REST 控制器

```java
@RestController
@RequestMapping("/api/your-module")
@Tag(name = "Your Module", description = "模块描述")
public class YourController {
    @Autowired
    private YourApplicationService service;

    // API 端点
}
```

### 编码规范

#### 命名规范

- **类名**：大驼峰命名法（PascalCase），如 `UserService`
- **方法名**：小驼峰命名法（camelCase），如 `getUserById`
- **常量**：全大写下划线分隔，如 `MAX_RETRY_COUNT`
- **包名**：全小写，如 `org.joker.comfypilot.user`

#### 代码组织

- **单一职责**：每个类只负责一个功能
- **依赖注入**：使用 `@Autowired` 进行依赖注入
- **异常处理**：使用自定义业务异常 `BusinessException`
- **日志记录**：使用 SLF4J，包含 TraceId 追踪

#### API 设计规范

- **RESTful 风格**：遵循 REST 设计原则
- **统一响应格式**：使用 `Result<T>` 包装响应数据
- **分页查询**：使用 `PageRequest` 和 `PageResponse`
- **Swagger 注解**：使用 `@Tag`、`@Operation` 等注解生成 API 文档

## 🔧 核心特性

### TraceId 日志追踪

系统自动为每个请求生成唯一的 TraceId，用于追踪请求的完整生命周期。

- **自动生成**：通过 `TraceIdInterceptor` 拦截器自动生成
- **日志输出**：所有日志自动包含 TraceId
- **异步支持**：通过 `TraceIdTaskDecorator` 支持异步任务追踪

详细说明请参考：[TraceId日志追踪系统使用说明.md](docs/architecture/TraceId日志追踪系统使用说明.md)

### 实体基类

所有领域实体继承 `BaseEntity`，提供统一的基础字段：

- **id**：主键（Long 类型）
- **createdAt**：创建时间
- **updatedAt**：更新时间
- **deleted**：逻辑删除标记

详细说明请参考：[实体基类使用说明.md](docs/architecture/实体基类使用说明.md)

### Redis 缓存

系统集成 Redis 作为缓存层，提供高性能数据访问：

- **缓存配置**：支持自定义序列化方式
- **缓存策略**：支持多种缓存策略
- **分布式锁**：支持分布式锁实现

详细说明请参考：[Redis配置说明.md](docs/architecture/Redis配置说明.md)

### 全局异常处理

通过 `GlobalExceptionHandler` 统一处理异常：

- **业务异常**：`BusinessException`
- **资源未找到**：`ResourceNotFoundException`
- **参数校验异常**：自动处理 `@Valid` 校验失败
- **统一响应格式**：所有异常返回统一的错误响应格式

### Swagger API 文档

系统集成 SpringDoc OpenAPI，自动生成 API 文档：

- **在线文档**：访问 `/swagger-ui.html` 查看交互式 API 文档
- **OpenAPI 规范**：访问 `/v3/api-docs` 获取 OpenAPI JSON
- **注解支持**：使用 `@Tag`、`@Operation`、`@Parameter` 等注解丰富文档

## 📚 文档说明

### 需求文档

完整的需求文档位于 [docs/requirements/](docs/requirements/) 目录：

- **项目概述**：了解项目定位和核心价值
- **核心数据模型**：查看所有数据表设计
- **API 设计**：查看所有 API 接口定义
- **模块设计**：查看各业务模块的详细设计

### 架构文档

架构相关文档位于 [docs/architecture/](docs/architecture/) 目录：

- **DDD 架构说明**：了解 DDD 四层架构设计
- **TraceId 日志追踪**：了解日志追踪系统使用方法
- **Redis 配置说明**：了解 Redis 缓存配置
- **实体基类说明**：了解实体基类使用方法

### API 文档

各模块的 API 文档位于 [docs/api/](docs/api/) 目录：

- **用户模块 API**：用户认证、权限管理相关接口
- **ComfyUI服务模块 API**：ComfyUI 服务管理相关接口
- **工作流模块 API**：工作流 CRUD 相关接口
- **会话模块 API**：AI 对话会话相关接口
- **Agent 模块 API**：Agent 配置和执行相关接口
- **AI 模型模块 API**：模型提供商和配置相关接口
- **资源模块 API**：文件上传和管理相关接口

## 🛠️ 技术规范

详细的技术规范请参考：[99-技术规范.md](docs/requirements/99-技术规范.md)

### 架构规范

- **DDD 四层架构**：接口层、应用层、领域层、基础设施层
- **模块化设计**：按业务领域划分模块
- **依赖倒置**：领域层不依赖基础设施层

### 代码规范

- **命名规范**：遵循 Java 命名约定
- **注释规范**：关键业务逻辑必须添加注释
- **异常处理**：使用自定义业务异常
- **日志规范**：使用 SLF4J，包含 TraceId

### 数据库规范

- **表命名**：小写下划线分隔，如 `comfyui_service`
- **字段命名**：小写下划线分隔，如 `created_at`
- **索引设计**：合理设计索引，提高查询性能
- **逻辑删除**：使用 `deleted` 字段标记删除

### API 规范

- **RESTful 设计**：遵循 REST 设计原则
- **统一响应**：使用 `Result<T>` 包装响应
- **错误处理**：返回统一的错误响应格式
- **版本管理**：通过 URL 路径管理 API 版本

## 🤝 贡献指南

### 开发流程

1. **Fork 项目**：Fork 本项目到你的 GitHub 账号
2. **创建分支**：创建功能分支 `git checkout -b feature/your-feature`
3. **提交代码**：提交你的修改 `git commit -m 'Add some feature'`
4. **推送分支**：推送到远程分支 `git push origin feature/your-feature`
5. **创建 PR**：创建 Pull Request

### 代码审查

- **代码质量**：确保代码符合项目规范
- **测试覆盖**：添加必要的单元测试
- **文档更新**：更新相关文档
- **提交信息**：使用清晰的提交信息

## 📝 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- **Issue**：在 GitHub 上提交 Issue
- **Email**：项目维护者邮箱

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

**版本信息**：v0.0.1-SNAPSHOT
**最后更新**：2026-01-15
