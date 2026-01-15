# DDD 架构说明

## 1. 架构概述

本项目采用 **领域驱动设计（Domain-Driven Design, DDD）** 架构模式，结合单体应用架构，使用 MyBatis-Plus + PostgreSQL 作为持久层技术栈。

## 2. 模块划分

项目按业务领域划分为以下核心模块：

### 2.1 common 模块
**职责**：公共基础设施

- `constant/`：常量定义
- `exception/`：异常定义
- `util/`：工具类
- `config/`：配置类

### 2.2 workflow 模块
**职责**：工作流管理

- 工作流 CRUD
- 工作流验证
- 工作流执行
- 工作流模板管理

### 2.3 user 模块
**职责**：用户管理

- 用户认证
- 权限管理
- 个人设置

### 2.4 aiagent 模块
**职责**：AI Agent 能力

- 自然语言理解
- 工作流生成
- 智能建议
- 对话管理

### 2.5 comfyui 模块
**职责**：ComfyUI 集成

- 实例管理
- 节点库管理
- 工作流转换
- 执行队列管理


## 3. Mapper XML 组织

```
src/main/resources/mapper/
├── workflow/          # 工作流模块 Mapper XML
├── user/             # 用户模块 Mapper XML
├── aiagent/          # AI Agent 模块 Mapper XML
└── comfyui/          # ComfyUI 集成模块 Mapper XML
```

每个模块的 Mapper XML 文件放在对应的目录下。

## 参考资料

- [领域驱动设计（DDD）](https://domain-driven-design.org/)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [PostgreSQL 官方文档](https://www.postgresql.org/docs/)
- [PGVector 文档](https://github.com/pgvector/pgvector)

