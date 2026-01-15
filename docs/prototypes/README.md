# ComfyUI Agent 代理系统 - 原型设计文档

## 📋 系统概述

本系统作为 ComfyUI 的代理层，统一管理多个 ComfyUI 服务实例，为用户提供增强的 AI Agent 辅助功能。

## 🎯 核心设计理念

### 1. 多对多关系模型

```
ComfyUI 服务 ←→ 工作流 ←→ Chat Agent Session
     (M)           (M)            (M)
```

- **ComfyUI 服务**：可以有多个工作流
- **工作流**：可以在多个 ComfyUI 服务上运行，可以有多个 Chat Session
- **Chat Agent Session**：可以关联多个工作流

### 2. 用户角色设计

#### 管理员用户
- 管理 ComfyUI 服务实例（增删改查）
- 查看服务状态和监控
- 管理用户权限

#### 普通用户
- 选择可用的 ComfyUI 服务
- 创建和编辑工作流
- 使用 AI Agent 辅助功能
- 管理自己的 Chat Session

### 3. 技术架构

#### 前端架构
```
┌─────────────────────────────────────────┐
│         用户界面层                        │
│  ┌──────────┐  ┌──────────┐  ┌────────┐ │
│  │ 登录页面  │  │ 服务选择  │  │ 管理后台│ │
│  └──────────┘  └──────────┘  └────────┘ │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         工作流编辑层                      │
│  ┌──────────────────────────────────┐   │
│  │  Session 管理 │ ComfyUI iframe   │   │
│  │  (左侧边栏)   │  (中间主区域)     │   │
│  │               │                  │   │
│  │               │  ┌─────────────┐ │   │
│  │               │  │ 悬浮聊天框   │ │   │
│  │               │  │ (可调整)     │ │   │
│  │               │  └─────────────┘ │   │
│  └──────────────────────────────────┘   │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         通信层                           │
│  ┌──────────┐  ┌──────────┐            │
│  │ postMessage│  │ WebSocket│            │
│  │ (iframe)  │  │ (Agent)  │            │
│  └──────────┘  └──────────┘            │
└─────────────────────────────────────────┘
```

#### iframe 通信机制
```javascript
// 父页面 → ComfyUI iframe
window.frames['comfyui'].postMessage({
  type: 'LOCK_WORKFLOW',
  payload: { reason: 'Agent editing' }
}, '*');

// ComfyUI iframe → 父页面
window.parent.postMessage({
  type: 'WORKFLOW_CHANGED',
  payload: { workflow: {...} }
}, '*');
```

## 📄 原型页面列表

### 1. `01-login.html` - 用户登录页面
**功能**:
- 用户名/密码登录
- 角色识别（管理员/普通用户）
- 基于角色自动跳转
  - 普通用户 → `03-user-service-selection.html`
  - 管理员 → `02-admin-dashboard.html`

**测试账号**:
- 普通用户: `user` / `password`
- 管理员: `admin` / `admin123`

---

### 2. `02-admin-service-management.html` - 管理员服务管理
**功能**:
- ComfyUI 服务列表展示
- 添加/编辑/删除服务
- 服务状态实时监控
- 连接测试功能
- 用户管理
- 系统统计数据

---

### 3. `03-user-service-selection.html` - 用户首页(服务选择)
**功能**:
- 显示所有可用的 ComfyUI 服务
- 服务状态实时监控(在线/离线)
- 服务统计信息(工作流数、会话数)
- 最近使用的服务快速访问

**新增功能**:
- 顶部导航栏
  - Logo (🤖 ComfyUI Agent)
  - "我的工作流"入口按钮
  - 用户头像下拉菜单
- 用户下拉菜单包含:
  - 用户基本信息显示
  - 个人信息链接
  - 我的工作流链接
  - 账号设置
  - 退出登录

**关键代码**:
```javascript
function toggleUserMenu() {
    const userMenu = document.getElementById('userMenu');
    userMenu.classList.toggle('active');
}

function goToWorkflows() {
    window.location.href = '03-1-user-workflows.html';
}

function goToProfile() {
    window.location.href = '03-2-user-profile.html';
}
```

---

### 4. `03-1-user-workflows.html` - 我的工作流 (新增)
**功能**:
- 展示用户所有工作流
- 网格/列表视图切换
- 搜索功能
- 筛选功能:
  - 全部工作流
  - 最近使用
  - 收藏的工作流
- 排序选项:
  - 最近使用
  - 名称
  - 创建时间
  - 修改时间

**工作流卡片信息**:
- 工作流名称和图标
- 所属 ComfyUI 服务
- 描述信息
- 会话数和版本数
- 最后使用时间
- 收藏状态(可切换)
- 快捷操作(编辑/删除)

**点击工作流行为**:
- 自动进入该工作流最后使用的 ComfyUI 服务
- 默认打开该工作流

**关键代码**:
```javascript
function openWorkflow(workflowId, serviceId) {
    // 自动进入对应服务并打开工作流
    alert(`打开工作流 #${workflowId}\n自动进入服务 #${serviceId}`);
    window.location.href = '04-workflow-editor-integrated.html';
}

function toggleFavorite(event, workflowId) {
    event.stopPropagation();
    const workflow = workflows.find(w => w.id === workflowId);
    if (workflow) {
        workflow.favorite = !workflow.favorite;
        renderWorkflows();
    }
}
```

---

### 5. `03-2-user-profile.html` - 个人信息页 (新增)
**功能**:
- 个人信息展示和编辑
  - 头像上传/更换
  - 显示名称
  - 邮箱地址
  - 手机号码
  - 注册时间(只读)
- 用户统计数据:
  - 工作流总数
  - 活跃会话数
  - 工作流版本数
  - 使用天数
- 账号安全:
  - 修改密码功能
  - 当前密码验证
  - 新密码确认
- 危险操作:
  - 删除账号(二次确认)

**关键特性**:
- 表单验证
- 实时保存提示
- 危险操作保护机制

---

### 6. `04-workflow-editor-integrated.html` - 工作流编辑器(核心页面)
**布局结构**:
```
┌─────────────────────────────────────────────────────────┐
│  [🤖] [工作流选择器▼] [服务状态] [保存] [导出]  [状态]  │
├──────────┬──────────────────────────────────────────────┤
│          │                                              │
│  会话    │                                              │
│  列表    │         ComfyUI iframe 区域                  │
│          │                                              │
│  [新建]  │                                              │
│          │                                              │
│  [返回]  │                                              │
└──────────┴──────────────────────────────────────────────┘
                                    ┌──────────────┐
                                    │  悬浮对话框  │
                                    │              │
                                    │  [最小化][×] │
                                    └──────────────┘
```

**核心功能**:

#### 顶部工具栏 (新增/增强)
- **Logo 按钮(🤖)**: 点击返回用户首页
  - 有未保存修改时弹出确认对话框
  - 提供保存选项
- **工作流选择器**: 下拉菜单显示当前服务的所有工作流
  - 显示工作流名称
  - 显示会话数和最后使用时间
  - 切换工作流时检查未保存修改
  - 动态渲染工作流列表
- **服务指示器**: 显示当前连接的 ComfyUI 服务
- **保存按钮**: 保存工作流修改(Ctrl+S)
  - 检测未保存状态
  - 快捷键支持
- **导出按钮**: 导出工作流文件
- **状态指示器**: 显示编辑器状态
  - 就绪状态(绿色)
  - Agent 编辑中(红色脉冲)

#### 左侧会话列表
- 显示当前工作流的所有 Agent 会话
- 会话信息:
  - 会话状态(进行中/已完成)
  - 会话标题
  - 关联的工作流名称
  - 最后操作摘要
  - 时间戳
- 点击会话:
  - 打开悬浮对话框
  - 加载该会话的历史消息
  - 加载该会话对应的工作流版本快照
  - 切换会话时检查未保存修改
- 新建会话按钮
- 返回服务选择按钮

#### 主编辑区域
- 嵌入 ComfyUI 原生界面(iframe)
- 工作流锁定机制:
  - Agent 编辑时自动锁定界面
  - 显示锁定遮罩和提示信息
  - 编辑完成后自动解锁
  - 防止用户和 Agent 同时编辑冲突
- 未保存修改提示:
  - 实时检测工作流变化
  - 顶部显示"有未保存的修改"警告
  - 切换工作流/返回首页时提示保存
  - 保存状态实时更新

#### 悬浮 Agent 对话框
- 可拖动定位
- 可调整大小(resize)
- 最小化/还原功能
- 关闭按钮
- 消息历史显示
- 输入框和发送按钮
- 工作流操作提示卡片
- 滚动到最新消息

**关键代码示例**:
```javascript
// 工作流选择器
function selectWorkflow(workflowId) {
    if (hasUnsavedChanges) {
        if (!confirm('⚠️ 有未保存的修改\n\n切换工作流将丢失未保存的修改\n\n确定要切换吗？')) {
            return;
        }
    }
    currentWorkflowId = workflowId;
    const workflow = workflows.find(w => w.id === workflowId);
    if (workflow) {
        currentWorkflowName = workflow.name;
        document.querySelector('.workflow-select-btn span:first-child').textContent = `📁 ${workflow.name}`;
        hasUnsavedChanges = false;
        updateWorkflowStatus();
        renderWorkflowDropdown();
    }
    toggleWorkflowDropdown();
}

// Logo 返回首页
function goHome() {
    if (hasUnsavedChanges) {
        if (!confirm('⚠️ 有未保存的修改\n\n返回首页将丢失未保存的修改\n\n确定要返回吗？')) {
            return;
        }
    }
    window.location.href = '03-user-service-selection.html';
}

// 工作流锁定/解锁
function lockWorkflow() {
    isWorkflowLocked = true;
    document.getElementById('comfyuiContainer').classList.add('locked');
    document.getElementById('statusText').textContent = 'Agent 编辑中';
}

function unlockWorkflow() {
    isWorkflowLocked = false;
    document.getElementById('comfyuiContainer').classList.remove('locked');
    document.getElementById('statusText').textContent = '就绪';
}
```

## 🔄 用户流程

### 管理员流程
```
登录 → 管理后台 → 服务管理 → 添加/编辑服务 → 保存
                ↓
              监控服务状态
```

### 普通用户流程

#### 流程 1: 新用户首次使用
```
登录页 → 服务选择页 → 选择服务 → 工作流编辑器 → 创建新工作流 → 与 Agent 对话 → 保存工作流
```

#### 流程 2: 查看和管理工作流
```
服务选择页 → 点击"我的工作流" → 工作流列表页
                                    ↓
                            [搜索/筛选/排序工作流]
                                    ↓
                            点击工作流卡片
                                    ↓
                    自动进入该工作流最后使用的服务
                                    ↓
                            工作流编辑器(已加载工作流)
```

#### 流程 3: 编辑现有工作流
```
工作流编辑器 → 选择工作流(下拉菜单) → 编辑工作流
                                        ↓
                                检测到未保存修改
                                        ↓
                                点击保存(Ctrl+S)
                                        ↓
                                    保存成功
```

#### 流程 4: 使用 Agent 辅助编辑
```
工作流编辑器 → 点击会话/新建会话 → 悬浮对话框打开
                                        ↓
                                输入需求对话
                                        ↓
                                Agent 理解需求
                                        ↓
                            工作流自动锁定(防止冲突)
                                        ↓
                            Agent 自动编辑工作流
                                        ↓
                            工作流自动解锁
                                        ↓
                            标记为"有未保存的修改"
                                        ↓
                                用户保存工作流
```

#### 流程 5: 会话管理
```
工作流编辑器 → 左侧会话列表 → 点击历史会话
                                    ↓
                            加载该会话的消息历史
                                    ↓
                            加载该会话对应的工作流版本快照
                                    ↓
                            继续对话或查看历史
                                    ↓
                            [可选] 保存为新版本
```

#### 流程 6: 个人信息管理
```
任意页面 → 点击用户头像 → 下拉菜单
                            ↓
                    选择"个人信息"
                            ↓
                    个人信息页面
                            ↓
            [编辑信息/修改密码/查看统计]
                            ↓
                        保存修改
```

## 🎨 设计规范

### 色彩方案
- **主色调**：`#3498db` (蓝色) - 主要操作按钮
- **辅助色**：`#2c3e50` (深蓝灰) - 侧边栏、导航
- **成功色**：`#27ae60` (绿色) - 状态指示
- **警告色**：`#f39c12` (橙色) - 警告提示
- **危险色**：`#e74c3c` (红色) - 删除、锁定
- **背景色**：`#1e1e1e` (深灰) - 工作区背景

### 组件规范
- **按钮圆角**：`6px`
- **卡片圆角**：`8px`
- **对话框圆角**：`12px`
- **字体大小**：标题 `16-20px`，正文 `13-14px`，辅助 `12px`
- **间距**：小 `8px`，中 `16px`，大 `24px`

### 响应式设计
- **最小宽度**：`1280px`（工作流编辑需要较大空间）
- **Session 侧边栏**：固定 `260px`
- **聊天框**：默认 `420x600px`，可调整

## 🔐 权限设计

### 管理员权限
- ✅ 管理 ComfyUI 服务
- ✅ 查看所有用户工作流
- ✅ 系统配置管理
- ✅ 使用所有普通用户功能

### 普通用户权限
- ✅ 选择可用服务
- ✅ 创建/编辑自己的工作流
- ✅ 使用 AI Agent 功能
- ✅ 管理自己的 Chat Session
- ❌ 无法访问管理后台

## 📊 数据模型（概念）

### ComfyUI 服务
```json
{
  "id": "uuid",
  "name": "生产环境 ComfyUI",
  "url": "http://192.168.1.100:8188",
  "status": "online|offline|error",
  "description": "高性能 GPU 服务器",
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### 工作流
```json
{
  "id": "uuid",
  "name": "SDXL 图像生成",
  "serviceId": "uuid",
  "userId": "uuid",
  "workflow": { /* ComfyUI workflow JSON */ },
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

### Chat Agent Session
```json
{
  "id": "uuid",
  "name": "SDXL 优化讨论",
  "userId": "uuid",
  "workflowId": "uuid",
  "messages": [
    {
      "role": "user|assistant",
      "content": "消息内容",
      "timestamp": "2025-01-15T10:00:00Z"
    }
  ],
  "createdAt": "2025-01-15T10:00:00Z",
  "updatedAt": "2025-01-15T10:00:00Z"
}
```

## 🚀 快速开始

### 1. 启动 ComfyUI 服务
```bash
cd ComfyUI
python main.py --listen 127.0.0.1 --port 8188
```

### 2. 打开原型页面
直接在浏览器中打开 `01-login.html` 开始体验完整流程。

### 3. 测试账号
- **普通用户**: `user` / `password`
- **管理员**: `admin` / `admin123`

### 4. 体验流程
1. 使用普通用户登录
2. 选择一个 ComfyUI 服务
3. 点击"我的工作流"查看工作流列表
4. 在工作流编辑器中体验 Agent 对话功能

---

## 📝 待实现功能

### 后端集成
- [ ] 用户认证 API (JWT)
- [ ] 工作流 CRUD API
- [ ] 会话管理 API
- [ ] 版本管理 API (Git-like 版本树)
- [ ] ComfyUI 服务状态监控
- [ ] WebSocket 实时通信

### 前端增强
- [ ] 工作流版本对比功能
- [ ] 批量操作(删除、导出)
- [ ] 高级搜索和筛选
- [ ] 工作流模板市场
- [ ] 拖拽上传工作流文件
- [ ] 工作流分享功能

### AI Agent 功能
- [ ] 自然语言工作流编辑
- [ ] 智能节点推荐
- [ ] 参数优化建议
- [ ] 错误诊断和修复
- [ ] 工作流性能分析

### 用户体验优化
- [ ] 暗色主题支持
- [ ] 键盘快捷键系统
- [ ] 撤销/重做功能
- [ ] 工作流预览缩略图
- [ ] 实时协作编辑

---

## 🚀 实现阶段

### Phase 1: 原型验证（当前 ✅）
- ✅ 创建静态原型页面
- ✅ 验证交互流程
- ✅ 新增用户导航系统
- ✅ 新增工作流管理页面
- ✅ 新增个人信息页面
- ✅ 增强工作流编辑器
- ✅ 收集用户反馈

### Phase 2: 后端开发
- 用户认证系统
- ComfyUI 服务管理 API
- 工作流存储 API
- Chat Session 管理 API
- 版本管理系统

### Phase 3: 前端开发
- React/Vue 实现界面
- iframe 通信机制
- WebSocket 实时通信
- 状态管理 (Vuex/Redux)
- 组件化重构

### Phase 4: AI Agent 集成
- Agent 工作流理解
- 自然语言转工作流操作
- 工作流优化建议
- 实时协作编辑

## 📝 注意事项

### iframe 跨域问题
- 需要 ComfyUI 服务配置 CORS
- 或使用反向代理统一域名
- postMessage 通信需要验证来源

### 工作流锁定机制
- Agent 编辑时锁定用户操作
- 显示明确的锁定状态
- 提供强制解锁选项（管理员）

### 性能考虑
- iframe 加载优化
- 大型工作流渲染性能
- 多 Session 消息存储

### 数据安全
- 工作流数据加密存储
- 用户权限隔离
- 敏感操作二次确认

---

## 🔧 技术栈

### 原型阶段 (当前)
- **前端**: 纯 HTML + CSS + JavaScript
- **UI 设计**: 自定义 CSS
- **通信**: postMessage (iframe) + HTTP API (模拟)
- **状态管理**: 原生 JavaScript

### 生产环境 (计划)
- **前端框架**: Vue 3 / React 18
- **UI 组件库**: Element Plus / Ant Design
- **状态管理**: Pinia / Redux Toolkit
- **通信**: WebSocket + RESTful API
- **后端**: Spring Boot (Java)
- **数据库**: PostgreSQL + Redis
- **认证**: JWT + Spring Security

---

## 📚 相关文档

- [ComfyUI 官方文档](https://github.com/comfyanonymous/ComfyUI)
- [iframe 嵌入测试](../test-comfyui/README.md)
- [API 接口文档](../../api/README.md) (待创建)
- [后端架构文档](../../docs/architecture/) (待创建)

---

## 🎯 核心特性总结

### 已实现 (原型阶段)
✅ 用户角色管理 (管理员/普通用户)
✅ 多 ComfyUI 服务管理
✅ 工作流列表和管理
✅ 个人信息页面
✅ 工作流编辑器集成
✅ Agent 对话系统
✅ 会话管理
✅ 工作流锁定机制
✅ 未保存修改检测
✅ 用户导航系统

### 待实现 (后续开发)
⏳ 后端 API 集成
⏳ 工作流版本管理
⏳ WebSocket 实时通信
⏳ AI Agent 智能编辑
⏳ 工作流模板市场
⏳ 协作编辑功能

---

## 🤝 贡献指南

1. 保持代码风格一致
2. 添加必要的注释
3. 测试所有交互功能
4. 更新相关文档
5. 遵循设计规范

---

## 📞 联系方式

如有问题或建议,请通过以下方式联系:
- 提交 Issue
- 发送邮件
- 项目讨论区

---

**文档版本**: v2.0
**创建日期**: 2025-01-15
**最后更新**: 2025-01-15
**维护者**: ComfyUI Agent Team
