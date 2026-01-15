# ComfyUI AI Agent 原型图

本目录包含 AI Agent 对话式工作流构建系统的所有原型图。

## 原型列表

### 1. 主对话界面 ([01-chat-interface.html](01-chat-interface.html))
- 左侧会话列表
- 主对话区域（用户与 AI 交互）
- 工作流状态预览
- 输入框

### 2. 工作流可视化视图 ([02-workflow-visual.html](02-workflow-visual.html))
- 节点画布
- 节点连接线
- 节点参数编辑
- 工具栏（执行、保存、导出）

### 3. AI 模型配置 ([03-model-config.html](03-model-config.html))
- 基础对话模型配置（OpenAI 协议）
- ComfyUI 专家模型配置
- RAG 知识库配置
- 连接测试

### 4. 工作流执行监控 ([04-execution-monitor.html](04-execution-monitor.html))
- 总体进度条
- 节点执行状态
- 实时日志
- 时间统计

### 5. 工作流模板库 ([05-workflow-templates.html](05-workflow-templates.html))
- 预设模板展示
- 分类筛选
- 搜索功能
- 模板详情

### 6. 节点参数编辑器 ([06-node-editor.html](06-node-editor.html))
- 参数表单
- 滑块控件
- 下拉选择
- 参数提示

### 7. ComfyUI 连接配置 ([07-comfyui-config.html](07-comfyui-config.html))
- 服务器地址配置
- 连接状态显示
- 健康检查设置
- 连接测试

## 使用说明

所有原型图都是独立的 HTML 文件，可以直接在浏览器中打开查看。

```bash
# 在浏览器中打开任意原型
open docs/prototypes/01-chat-interface.html
```

## 设计原则

- 简洁明了的界面
- 清晰的信息层级
- 符合现代 Web 应用设计规范
- 响应式布局
- 无外部依赖（纯 HTML/CSS/JS）
