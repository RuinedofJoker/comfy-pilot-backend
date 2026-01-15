# ComfyUI iframe 嵌入测试套件

## 📋 测试目标

验证 ComfyUI 是否能被 iframe 嵌入，以及如何与平台进行通信和数据交互。

## 🧪 测试文件说明

### 1. `01-basic-iframe-test.html` - 基础 iframe 嵌入测试

**测试目的：**
- 验证 ComfyUI 是否允许被 iframe 嵌入
- 检测是否有 X-Frame-Options 或 CSP 限制
- 监控加载状态和错误

**如何使用：**
1. 确保 ComfyUI 服务运行在 `http://127.0.0.1:8188/`
2. 在浏览器中打开此文件
3. 观察 iframe 是否能正常显示 ComfyUI 界面

**成功标志：**
- ✅ iframe 中能看到 ComfyUI 的节点编辑器
- ✅ 加载状态显示"成功"
- ✅ 可以在 iframe 中正常操作

**失败标志：**
- ❌ iframe 显示空白
- ❌ 浏览器控制台显示 X-Frame-Options 错误
- ❌ 显示"拒绝连接"或"无法加载"

---

### 2. `02-postmessage-test.html` - postMessage 通信测试

**测试目的：**
- 验证父页面与 ComfyUI iframe 之间的 postMessage 通信
- 测试消息发送和接收机制
- 检查跨域通信限制

**如何使用：**
1. 在浏览器中打开此文件
2. 点击"发送测试消息"按钮
3. 观察消息日志中是否有响应

**成功标志：**
- ✅ 能发送消息到 iframe
- ✅ 能接收到 iframe 的响应消息
- ✅ 消息日志显示双向通信

**失败标志：**
- ❌ 发送消息后无响应
- ❌ 控制台显示跨域错误
- ❌ 无法建立通信

**注意事项：**
- ComfyUI 原生可能不支持 postMessage，需要修改或扩展
- 如果无响应，说明需要在 ComfyUI 中添加消息监听器

---

### 3. `03-api-test.html` - ComfyUI API 调用测试

**测试目的：**
- 验证是否能通过 HTTP API 直接与 ComfyUI 通信
- 测试常用 API 端点的可用性
- 检查 CORS 跨域限制

**如何使用：**
1. 在浏览器中打开此文件
2. 点击各个 API 测试按钮
3. 观察响应数据

**测试的 API 端点：**
- `/system_stats` - 系统状态
- `/object_info` - 节点信息
- `/history` - 历史记录
- `/queue` - 队列状态
- `/prompt` - 提交工作流

**成功标志：**
- ✅ API 返回 JSON 数据
- ✅ 状态显示"成功"
- ✅ 能看到详细的响应内容

**失败标志：**
- ❌ CORS 错误（跨域限制）
- ❌ 404 Not Found
- ❌ 连接被拒绝

**解决 CORS 问题：**
如果遇到 CORS 错误，需要配置 ComfyUI 允许跨域：
```python
# 在 ComfyUI 的服务器配置中添加 CORS 头
Access-Control-Allow-Origin: *
```

---

### 4. `04-integrated-test.html` - 综合集成测试

**测试目的：**
- 模拟真实平台环境
- 综合测试 iframe 嵌入 + postMessage + API 调用
- 验证完整的工作流操作流程

**如何使用：**
1. 在浏览器中打开此文件
2. 使用左侧控制面板测试各项功能
3. 观察日志输出和 iframe 响应

**功能测试：**
- 📤 postMessage 通信
- 🔌 API 调用
- 💾 工作流保存/加载

**成功标志：**
- ✅ iframe 正常显示 ComfyUI
- ✅ 能发送和接收消息
- ✅ API 调用成功
- ✅ 日志显示所有操作正常

---

## 🚀 快速开始

### 前置条件

1. **启动 ComfyUI 服务**
   ```bash
   # 在 ComfyUI 目录下运行
   python main.py --listen 127.0.0.1 --port 8188
   ```

2. **确认服务运行**
   - 在浏览器中访问 `http://127.0.0.1:8188/`
   - 确保能看到 ComfyUI 界面

### 测试步骤

1. **按顺序测试**
   ```
   01-basic-iframe-test.html  →  基础嵌入
   02-postmessage-test.html   →  消息通信
   03-api-test.html           →  API 调用
   04-integrated-test.html    →  综合测试
   ```

2. **记录测试结果**
   - 每个测试的成功/失败状态
   - 遇到的错误信息
   - 浏览器控制台的输出

---

## 📊 预期测试结果

### 场景 A：ComfyUI 允许 iframe 嵌入（理想情况）

| 测试项 | 结果 | 说明 |
|--------|------|------|
| 基础 iframe 嵌入 | ✅ 成功 | 可以直接使用 iframe |
| postMessage 通信 | ⚠️ 需要扩展 | 需要在 ComfyUI 中添加监听器 |
| API 调用 | ✅ 成功 | 可以通过 API 操作工作流 |

**推荐方案：** iframe + API 混合模式
- 使用 iframe 显示 ComfyUI 界面
- 使用 API 进行数据交互和工作流管理
- 平台拦截保存操作，保存到平台数据库

---

### 场景 B：ComfyUI 禁止 iframe 嵌入

| 测试项 | 结果 | 说明 |
|--------|------|------|
| 基础 iframe 嵌入 | ❌ 失败 | X-Frame-Options: DENY |
| postMessage 通信 | ❌ 无法测试 | iframe 无法加载 |
| API 调用 | ✅ 成功 | API 仍然可用 |

**解决方案：**

**方案 1：反向代理 + 修改响应头**
```nginx
# Nginx 配置示例
location /comfyui/ {
    proxy_pass http://127.0.0.1:8188/;
    proxy_hide_header X-Frame-Options;
    add_header X-Frame-Options "ALLOWALL";
}
```

**方案 2：修改 ComfyUI 源码**
- 找到设置 X-Frame-Options 的代码
- 修改为允许 iframe 嵌入

**方案 3：纯 API 模式 + 自定义前端**
- 不使用 iframe
- 通过 API 获取工作流数据
- 自己实现工作流编辑器（工作量大）

---

## 🔍 常见问题

### Q1: iframe 显示空白怎么办？

**可能原因：**
1. ComfyUI 服务未启动
2. X-Frame-Options 限制
3. HTTPS 混合内容问题

**解决方法：**
1. 检查 `http://127.0.0.1:8188/` 是否能直接访问
2. 查看浏览器控制台错误信息
3. 尝试使用反向代理

---

### Q2: API 调用出现 CORS 错误？

**错误信息：**
```
Access to fetch at 'http://127.0.0.1:8188/api' from origin 'null'
has been blocked by CORS policy
```

**解决方法：**
1. 配置 ComfyUI 允许跨域
2. 使用反向代理统一域名
3. 在开发环境使用浏览器插件临时禁用 CORS

---

### Q3: postMessage 无响应？

**原因：**
ComfyUI 原生不支持 postMessage 通信

**解决方法：**
1. 在 ComfyUI 中添加消息监听器
2. 或者完全依赖 API 进行通信
3. 使用 WebSocket 作为替代方案

---

## 📝 测试报告模板

完成测试后，请填写以下报告：

```markdown
## ComfyUI iframe 嵌入测试报告

**测试日期：** YYYY-MM-DD
**ComfyUI 版本：** vX.X.X
**浏览器：** Chrome/Firefox/Safari vXX

### 测试结果

| 测试项 | 结果 | 备注 |
|--------|------|------|
| 基础 iframe 嵌入 | ✅/❌ |  |
| postMessage 通信 | ✅/❌ |  |
| API 调用 | ✅/❌ |  |
| 综合集成 | ✅/❌ |  |

### 遇到的问题

1. 问题描述...
2. 错误信息...

### 推荐方案

基于测试结果，推荐使用：
- [ ] 方案 A: 直接 iframe 嵌入
- [ ] 方案 B: 反向代理 + iframe
- [ ] 方案 C: 纯 API 模式

### 下一步行动

1. ...
2. ...
```

---

## 🎯 下一步

根据测试结果，我们将：

1. **如果 iframe 可用：**
   - 修改原型文件，添加工作流选择器
   - 实现平台工作流与 ComfyUI 的同步
   - 设计保存拦截机制

2. **如果 iframe 不可用：**
   - 配置反向代理
   - 或考虑其他技术方案

请先完成测试，然后告诉我结果，我会据此调整原型设计！ 🚀
