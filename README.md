# ComfyUI Pilot 后端服务

ComfyUI 工作流管理和 AI 辅助编辑系统的后端服务。

## 前端仓库地址

[前端仓库地址](https://github.com/RuinedofJoker/comfy-pilot-h5)

## 功能概述

- **统一管理**: 集中管理多个 ComfyUI 服务实例
- **AI 辅助编辑**: 通过自然语言对话创建和修改工作流
- **Skills 扩展**: 支持可复用的 AI 能力模块

## 快速开始

### 环境要求

- Java 17+（推荐21）
- PostgreSQL 12+
- Redis 5+
- Maven 3.6+

### 配置数据库

1. 创建 PostgreSQL 数据库：
```sql
CREATE DATABASE comfy_pilot;
```

2. 执行初始化脚本（按顺序执行 `docs/sql/` 目录下的 SQL 文件）：
```bash
psql -U your_user -d comfy_pilot -f docs/sql/create_user_table.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_permission_tables.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_comfyui_server_table.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_workflow_tables.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_session_tables.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_agent_tables.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_model_tables.sql
psql -U your_user -d comfy_pilot -f docs/sql/create_resource_and_notification_tables.sql
```

### 配置环境变量

复制 `.env.example` 并改名为 `.env` 文件或设置系统环境变量：

```bash
# 数据库配置
DB_DRIVER=org.postgresql.Driver
DB_URL=jdbc:postgresql://localhost:5432/comfy_pilot
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
REDIS_DB=0

# 其他配置项见application.yml
```

### 启动服务

```bash
# 编译
mvn clean package

# 运行
java -jar target/comfy-pilot-backend-0.0.1-SNAPSHOT.jar
```

服务将在 `http://localhost:8080` 启动。

## 核心配置

### Skills 配置

在 `src/main/resources/application.yml` 中配置 Skills 目录：

```yaml
skills:
  directories:
    - D:/code/comfy-pilot/skills
    - /path/to/custom-skills  # 可配置多个目录
```

Skills 是可复用的 AI 能力模块，详见 [Skills 快速指南](docs/Skills快速指南.md)。

### ComfyUI 服务配置

启动后通过管理员账号在 Web 界面添加 ComfyUI 服务：
- 配置 ComfyUI 的访问地址（如 `http://localhost:8188`）

高级配置：
- 允许Agent在ComfyUI服务所在机器上执行命令
- **本地服务**: ComfyUI服务在本机上运行，将直接执行本地命令
- **远程服务**: 配置 SSH 连接信息和 ComfyUI 地址

### AI 模型配置

在管理界面配置 AI 模型提供商（可选）：
- OpenAI（GPT-4、GPT-3.5 等）

### 项目结构

```
src/main/java/org/joker/comfypilot/
├── common/           # 通用组件（工具、配置）
├── user/             # 用户模块
├── auth/             # 认证模块
├── permission/       # 权限模块
├── cfsvr/            # ComfyUI 服务模块
├── workflow/         # 工作流模块
├── session/          # 会话模块
├── agent/            # Agent 模块
├── model/            # AI 模型模块
├── resource/         # 资源模块
├── notification/     # 通知模块
├── monitor/          # 监控模块
├── audit/            # 审计日志模块
└── tool/             # 工具模块
```

每个模块采用 DDD 四层架构：
- `interfaces/` - 接口层（Controller）
- `application/` - 应用层（Service、DTO）
- `domain/` - 领域层（Entity、Repository）
- `infrastructure/` - 基础设施层（Mapper、PO）

### 技术栈

- **框架**: Spring Boot 3.x
- **数据库**: PostgreSQL + MyBatis-Plus
- **缓存**: Redis
- **安全**: JWT + Spring Security
- **AI 框架**: LangChain4j
- **通信**: WebSocket + RESTful API

### 详细文档

- [项目概述](docs/requirements/00-项目概述.md)
- [技术规范](docs/requirements/99-技术规范.md)
- [架构说明](docs/architecture/DDD架构说明.md)
- [Skills 快速指南](docs/Skills快速指南.md)
- [模块设计文档](docs/requirements/)

## API 文档

启动服务后访问 Swagger UI：
```
http://localhost:8080/swagger-ui.html
```

## 常见问题

### ComfyUI 服务连接失败
- 确认 ComfyUI 服务已启动
- 如果使用反向代理访问ComfyUI访问，检查是否配置了 WebSocket 的代理
- 对于远程服务，检查 SSH 连接配置

### AI 模型调用失败
- 检查 API Key 是否正确
- 确认网络连接正常
- 查看模型配置是否启用

## 许可证

[GPL-3.0 License](LICENSE)

## 支持

如有问题，请提交 Issue 或联系开发团队。
