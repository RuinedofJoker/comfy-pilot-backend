# Agent模块设计

## 模块职责
- Agent 配置管理
- Agent 任务调度与执行
- 工具调用管理

## 核心组件

### Domain 层
- **AgentConfig**: Agent配置实体
- **AgentExecution**: Agent执行记录实体
- **AgentRepository**: Agent仓储接口

### Infrastructure 层
- **AgentRepositoryImpl**: Agent仓储实现
- **AgentMapper**: MyBatis-Plus Mapper
- **ToolRegistry**: 工具注册表

### Application 层
- **AgentService**: Agent服务
  - Agent配置CRUD
  - Agent状态管理
- **AgentExecutor**: Agent执行器
  - 任务执行
  - 工具调用
  - 结果处理
- **AgentScheduler**: 任务调度器
  - 任务队列管理
  - 并发控制

### Interface 层
- **AgentController**: Agent管理接口

## Agent类型

### WORKFLOW_EDITOR (工作流编辑)
- 理解用户编辑需求
- 分析工作流结构
- 生成修改指令
- 验证修改结果

### WORKFLOW_ANALYZER (工作流分析)
- 性能分析
- 问题诊断
- 优化建议

### GENERAL_ASSISTANT (通用助手)
- 回答问题
- 使用指导
- 概念解释

## 工具定义

### 工具接口示例
```json
{
  "name": "add_node",
  "description": "添加节点",
  "parameters": {
    "type": "object",
    "properties": {
      "node_type": {"type": "string"},
      "position": {"type": "object"},
      "params": {"type": "object"}
    },
    "required": ["node_type"]
  }
}
```

## 核心流程

### Agent执行流程
1. 接收用户消息
2. 选择合适的Agent
3. 构建执行上下文
4. 调用AI模型
5. 解析工具调用
6. 执行工具操作
7. 返回执行结果

### 工作流编辑流程
1. Agent分析用户意图
2. 锁定工作流
3. 调用编辑工具
4. 验证修改结果
5. 解锁工作流
6. 创建版本快照

## 性能优化
- Agent配置缓存(Redis)
- 并发控制: 单Agent最大5个并发
- Token优化: 历史消息截断(保留10条)
