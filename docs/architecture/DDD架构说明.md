# DDD 架构说明

## 1. 架构概述

本项目采用 **领域驱动设计（Domain-Driven Design, DDD）** 架构模式，结合单体应用架构，使用 MyBatis-Plus + PostgreSQL 作为持久层技术栈。

## 2. 分层架构

项目采用经典的 DDD 四层架构：

```
org.joker.comfypilot.{module}
├── interfaces/          # 接口层（用户接口层）
│   ├── controller/      # REST API 控制器
│   └── assembler/       # DTO 转换器
├── application/         # 应用层
│   ├── service/         # 应用服务
│   └── dto/            # 数据传输对象
├── domain/             # 领域层
│   ├── entity/         # 领域实体
│   └── repository/     # 仓储接口
└── infrastructure/     # 基础设施层
    └── persistence/    # 持久化实现
        ├── mapper/     # MyBatis Mapper 接口
        └── po/         # 持久化对象
```

### 2.1 接口层（Interfaces Layer）

**职责**：处理用户请求，暴露 API 接口

- **controller/**：REST API 控制器，处理 HTTP 请求
- **assembler/**：负责 DTO 与领域对象之间的转换

**示例**：
```java
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    // 处理工作流相关的 HTTP 请求
}
```

### 2.2 应用层（Application Layer）

**职责**：编排业务流程，协调领域对象完成业务用例

- **service/**：应用服务，实现具体的业务用例
- **dto/**：数据传输对象，用于层间数据传递

**示例**：
```java
@Service
public class WorkflowApplicationService {
    // 编排工作流创建、修改等业务流程
}
```

### 2.3 领域层（Domain Layer）

**职责**：核心业务逻辑，包含领域模型和业务规则

- **entity/**：领域实体，包含业务逻辑和业务规则
- **repository/**：仓储接口，定义数据访问契约

**示例**：
```java
public class Workflow {
    // 工作流领域实体，包含业务逻辑
}

public interface WorkflowRepository {
    // 工作流仓储接口
}
```

### 2.4 基础设施层（Infrastructure Layer）

**职责**：提供技术支持，实现领域层定义的接口

- **persistence/mapper/**：MyBatis Mapper 接口
- **persistence/po/**：持久化对象（与数据库表对应）

**示例**：
```java
@Mapper
public interface WorkflowMapper extends BaseMapper<WorkflowPO> {
    // MyBatis-Plus Mapper
}
```

## 3. 模块划分

项目按业务领域划分为以下核心模块：

### 3.1 common 模块
**职责**：公共基础设施

- `constant/`：常量定义
- `exception/`：异常定义
- `util/`：工具类
- `config/`：配置类

### 3.2 workflow 模块
**职责**：工作流管理

- 工作流 CRUD
- 工作流验证
- 工作流执行
- 工作流模板管理

### 3.3 user 模块
**职责**：用户管理

- 用户认证
- 权限管理
- 个人设置

### 3.4 aiagent 模块
**职责**：AI Agent 能力

- 自然语言理解
- 工作流生成
- 智能建议
- 对话管理

### 3.5 version 模块
**职责**：版本控制

- 版本管理
- 差异对比
- 版本回滚
- 分支管理

### 3.6 resource 模块
**职责**：资源管理

- 文件上传
- 资源存储
- 资源引用管理

### 3.7 comfyui 模块
**职责**：ComfyUI 集成

- 实例管理
- 节点库管理
- 工作流转换
- 执行队列管理

## 4. 数据流转

```
HTTP 请求
    ↓
Controller (接口层)
    ↓ DTO
ApplicationService (应用层)
    ↓ Entity
Domain Entity (领域层)
    ↓ Repository Interface
Repository Impl (基础设施层)
    ↓ Mapper
MyBatis-Plus Mapper
    ↓ PO
PostgreSQL 数据库
```

## 5. 对象转换规则

### 5.1 DTO (Data Transfer Object)
- 用于接口层和应用层之间的数据传输
- 面向外部接口设计
- 可包含多个实体的组合数据

### 5.2 Entity (领域实体)
- 领域层的核心对象
- 包含业务逻辑和业务规则
- 具有唯一标识

### 5.3 PO (Persistent Object)
- 与数据库表一一对应
- 仅用于持久化
- 不包含业务逻辑

### 5.4 转换流程

```
外部请求 → DTO → Entity → PO → 数据库
数据库 → PO → Entity → DTO → 外部响应
```

## 6. MyBatis-Plus 配置

### 6.1 核心配置

```yaml
mybatis-plus:
  # Mapper XML 文件位置
  mapper-locations: classpath*:/mapper/**/*.xml

  # 实体类扫描路径（支持通配符）
  type-aliases-package: org.joker.comfypilot.*.domain.entity

  configuration:
    # 驼峰命名转换
    map-underscore-to-camel-case: true
    # SQL 日志输出
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

  global-config:
    db-config:
      # 主键类型（雪花算法）
      id-type: ASSIGN_ID
      # 逻辑删除配置
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 6.2 Mapper 扫描

```java
@MapperScan("org.joker.comfypilot.*.infrastructure.persistence.mapper")
```

使用通配符 `*` 扫描所有模块的 Mapper 接口。

### 6.3 分页插件

已配置 PostgreSQL 分页插件：
- 最大单页限制：1000 条
- 超出最大页不回到首页

## 7. 数据库设计

### 7.1 PostgreSQL 配置

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://192.168.78.15:5432/comfy_pilot
    username: admin
    password: 123456
```

### 7.2 PGVector 支持

项目已集成 PGVector 扩展，用于向量相似度搜索：
- 版本：0.1.6
- 用途：AI 知识库、工作流相似度匹配

### 7.3 连接池配置

使用 Hikari 连接池：
- 最小空闲连接：5
- 最大连接数：20
- 空闲超时：10 分钟
- 连接超时：30 秒
- 最大生命周期：30 分钟

## 8. 开发规范

### 8.1 命名规范

- **包名**：全小写，使用点分隔
- **类名**：大驼峰（PascalCase）
- **方法名**：小驼峰（camelCase）
- **常量**：全大写，下划线分隔

### 8.2 分层调用规则

```
✅ 允许的调用方向：
Controller → ApplicationService → DomainEntity → Repository

❌ 禁止的调用：
- 跨层调用（如 Controller 直接调用 Repository）
- 反向调用（如 Domain 调用 Application）
- 基础设施层调用领域层
```

### 8.3 事务管理

- 事务边界设置在 **应用层（ApplicationService）**
- 使用 `@Transactional` 注解
- 领域层不直接处理事务

### 8.4 异常处理

- 领域层抛出领域异常
- 应用层捕获并转换为应用异常
- 接口层统一处理异常并返回标准响应

## 9. Mapper XML 组织

```
src/main/resources/mapper/
├── workflow/          # 工作流模块 Mapper XML
├── user/             # 用户模块 Mapper XML
├── aiagent/          # AI Agent 模块 Mapper XML
├── version/          # 版本控制模块 Mapper XML
├── resource/         # 资源管理模块 Mapper XML
└── comfyui/          # ComfyUI 集成模块 Mapper XML
```

每个模块的 Mapper XML 文件放在对应的目录下。

## 10. 下一步开发指南

### 10.1 创建新功能的步骤

1. **定义领域实体**（domain/entity）
2. **定义仓储接口**（domain/repository）
3. **创建持久化对象**（infrastructure/persistence/po）
4. **实现 Mapper 接口**（infrastructure/persistence/mapper）
5. **编写 Mapper XML**（resources/mapper）
6. **实现仓储接口**（infrastructure/persistence/repository）
7. **创建应用服务**（application/service）
8. **定义 DTO**（application/dto）
9. **创建控制器**（interfaces/controller）
10. **编写 Assembler**（interfaces/assembler）

### 10.2 示例：创建工作流功能

```java
// 1. 领域实体
public class Workflow {
    private Long id;
    private String name;
    // 业务逻辑方法
}

// 2. 仓储接口
public interface WorkflowRepository {
    Workflow save(Workflow workflow);
    Workflow findById(Long id);
}

// 3. 持久化对象
@TableName("workflow")
public class WorkflowPO {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
}

// 4. Mapper 接口
@Mapper
public interface WorkflowMapper extends BaseMapper<WorkflowPO> {
}

// 5. 仓储实现
@Repository
public class WorkflowRepositoryImpl implements WorkflowRepository {
    @Autowired
    private WorkflowMapper workflowMapper;
}

// 6. 应用服务
@Service
public class WorkflowApplicationService {
    @Autowired
    private WorkflowRepository workflowRepository;

    @Transactional
    public WorkflowDTO createWorkflow(CreateWorkflowDTO dto) {
        // 业务逻辑
    }
}

// 7. 控制器
@RestController
@RequestMapping("/api/v1/workflows")
public class WorkflowController {
    @Autowired
    private WorkflowApplicationService workflowApplicationService;

    @PostMapping
    public Result<WorkflowDTO> create(@RequestBody CreateWorkflowDTO dto) {
        return Result.success(workflowApplicationService.createWorkflow(dto));
    }
}
```

## 11. 参考资料

- [领域驱动设计（DDD）](https://domain-driven-design.org/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)
- [PGVector 文档](https://github.com/pgvector/pgvector)

---

**文档版本**：v1.0
**创建日期**：2026-01-14
**维护者**：开发团队
