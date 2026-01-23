# ComfyPilot Agent WebSocket 接口文档

## 1. 连接认证

### 1.1 连接URL
```
ws://your-domain/ws/chat/{sessionCode}
```

### 1.2 认证方式
**方式1：请求头认证（推荐）**

```typescript
const ws = new WebSocket(`ws://localhost:8080/ws/chat/${SESSION_CODE}`, {
    headers: {
        'Authorization': 'Bearer YOUR_TOKEN'
    }
});
```

**方式2：查询参数认证**
```typescript
const ws = new WebSocket('ws://localhost:8080/ws/chat/SESSION_CODE?token=YOUR_TOKEN');
```

### 1.3 认证流程
1. 服务端从请求头或查询参数获取Token
2. 验证Token有效性并获取用户信息
3. 验证sessionCode是否属于该用户
4. 认证成功后建立连接，失败返回401/403

---

## 2. 消息交互机制

### 2.1 核心概念

**会话标识**
- `sessionCode`: 聊天会话唯一标识
- `requestId`: 单次请求唯一标识（时间戳）

**消息方向**
- **C→S**: 客户端发送给服务端
- **S→C**: 服务端推送给客户端

**执行模型**
- 完全异步响应式
- 基于事件驱动
- 支持流式输出
- 支持中断控制

### 2.2 消息类型总览

| 消息类型 | 方向 | 说明 |
|---------|------|------|
| `USER_MESSAGE` | C→S | 用户发送消息 |
| `USER_ORDER` | C→S | 用户发送命令 |
| `AGENT_TOOL_CALL_RESPONSE` | C→S | 工具调用响应 |
| `INTERRUPT` | C→S | 中断执行 |
| `PING` | C→S | 心跳检测 |
| `AGENT_PROMPT` | S→C | Agent状态提示 |
| `AGENT_STREAM` | S→C | Agent流式输出 |
| `AGENT_COMPLETE` | S→C | Agent完成 |
| `AGENT_TOOL_CALL_REQUEST` | S→C | 工具调用请求 |
| `PONG` | S→C | 心跳响应 |

---

## 3. 完整交互流程

### 3.1 标准对话流程

```
用户 → [USER_MESSAGE] → 服务端
                          ↓
                    [AGENT_PROMPT: THINKING]
                          ↓
                    [AGENT_STREAM] × N (流式输出)
                          ↓
                    [AGENT_COMPLETE]
```

### 3.2 工具调用流程

```
用户 → [USER_MESSAGE] → 服务端
                          ↓
                    [AGENT_PROMPT: THINKING]
                          ↓
                    [AGENT_PROMPT: TOOL_CALLING]
                          ↓
                    [AGENT_TOOL_CALL_REQUEST] → 客户端
                                                  ↓
                                            执行工具/用户确认
                                                  ↓
客户端 ← [AGENT_TOOL_CALL_RESPONSE] ← 服务端
                          ↓
                    [AGENT_PROMPT: TOOL_COMPLETE]
                          ↓
                    [AGENT_STREAM] × N
                          ↓
                    [AGENT_COMPLETE]
```

### 3.3 中断流程

```
用户 → [INTERRUPT] → 服务端
                       ↓
                 [AGENT_PROMPT: INTERRUPTED]
                       ↓
                 [AGENT_COMPLETE]
```

### 3.4 错误处理流程

```
服务端异常
    ↓
[AGENT_PROMPT: ERROR]
    ↓
[AGENT_COMPLETE]
```

---

## 4. 消息详细说明

### 4.1 USER_MESSAGE（用户消息）

**发送时机**: 用户输入消息并发送

**消息结构**:
```typescript
{
  type: 'USER_MESSAGE',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  content: '用户输入的文本',
  data: {
    workflowContent: '{"nodes": [...]}', // ComfyUI工作流JSON
    toolSchemas: [...]  // 可选：客户端MCP工具列表
  },
  timestamp: 1737705600000
}
```

**服务端响应**:
- `AGENT_PROMPT` (THINKING)
- `AGENT_STREAM` × N
- `AGENT_COMPLETE` 或 `AGENT_TOOL_CALL_REQUEST`

---

### 4.2 AGENT_PROMPT（状态提示）

**接收时机**: Agent执行过程中的状态变化

**消息结构**:
```typescript
{
  type: 'AGENT_PROMPT',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  data: {
    promptType: 'THINKING' | 'TOOL_CALLING' | 'TOOL_COMPLETE' |
                'SUMMARY' | 'SUMMARY_COMPLETE' | 'INTERRUPTED' | 'ERROR',
    message: '可选的自定义提示内容'
  },
  timestamp: 1737705600000
}
```

**提示类型说明**:
- `THINKING`: Agent开始思考
- `TOOL_CALLING`: 正在调用工具
- `TOOL_COMPLETE`: 工具调用完成
- `SUMMARY`: 生成摘要中
- `SUMMARY_COMPLETE`: 摘要完成
- `INTERRUPTED`: 执行被中断
- `ERROR`: 执行错误

**前端处理**: 显示状态指示器/加载动画

---

### 4.3 AGENT_STREAM（流式输出）

**接收时机**: Agent生成内容时实时推送

**消息结构**:
```typescript
{
  type: 'AGENT_STREAM',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  content: '部分输出内容',
  timestamp: 1737705600000
}
```

**前端处理**:
```typescript
let fullContent = '';
ws.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  if (msg.type === 'AGENT_STREAM') {
    fullContent += msg.content;
    updateUI(fullContent); // 实时更新UI
  }
};
```

---

### 4.4 AGENT_TOOL_CALL_REQUEST（工具调用请求）

**接收时机**: Agent需要调用工具时

**消息结构**:
```typescript
{
  type: 'AGENT_TOOL_CALL_REQUEST',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  data: {
    toolName: 'readFile',
    toolArgs: '{"path": "/path/to/file"}',
    isClientTool: true  // true=客户端工具, false=服务端工具
  },
  timestamp: 1737705600000
}
```

**前端处理逻辑**:
```typescript
if (msg.data.isClientTool) {
  // 客户端工具：执行并返回结果
  const result = await executeClientTool(msg.data.toolName, msg.data.toolArgs);
  sendToolResponse(true, result, true);
} else {
  // 服务端工具：仅需用户确认
  const allow = await askUserConfirm(msg.data.toolName);
  sendToolResponse(allow, null, false);
}
```

---

### 4.5 AGENT_TOOL_CALL_RESPONSE（工具调用响应）

**发送时机**: 收到工具调用请求后

**客户端工具响应**:
```typescript
{
  type: 'AGENT_TOOL_CALL_RESPONSE',
  sessionCode: 'xxx',
  requestId: '1737705600000', // 使用请求的requestId
  data: {
    toolName: 'readFile',
    isClientTool: true,
    toolArgs: '{"path": "/path/to/file"}',
    isAllow: true,
    success: true,
    result: '{"content": "file content"}'
  },
  timestamp: 1737705600000
}
```

**服务端工具响应（仅确认）**:
```typescript
{
  type: 'AGENT_TOOL_CALL_RESPONSE',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  data: {
    toolName: 'updateStatus',
    isClientTool: false,
    toolArgs: '{"status": "active"}',
    isAllow: true  // 或 false 拒绝
  },
  timestamp: 1737705600000
}
```

**错误响应**:
```typescript
{
  type: 'AGENT_TOOL_CALL_RESPONSE',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  data: {
    toolName: 'readFile',
    isClientTool: true,
    toolArgs: '{"path": "/path/to/file"}',
    isAllow: true,
    success: false,
    error: 'File not found'
  },
  timestamp: 1737705600000
}
```

---

### 4.6 AGENT_COMPLETE（完成）

**接收时机**: Agent执行完成（成功/失败/中断）

**消息结构**:
```typescript
{
  type: 'AGENT_COMPLETE',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  data: {},
  timestamp: 1737705600000
}
```

**前端处理**:
- 停止加载动画
- 重置输入框状态
- 允许发送新消息

---

### 4.7 INTERRUPT（中断）

**发送时机**: 用户点击停止按钮

**消息结构**:
```typescript
{
  type: 'INTERRUPT',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  timestamp: 1737705600000
}
```

**服务端响应**:
- `AGENT_PROMPT` (INTERRUPTED)
- `AGENT_COMPLETE`

---

### 4.8 PING/PONG（心跳）

**发送时机**: 定时发送（建议30秒）

**PING消息**:
```typescript
{
  type: 'PING',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  timestamp: 1737705600000
}
```

**PONG响应**:
```typescript
{
  type: 'PONG',
  sessionCode: 'xxx',
  requestId: '1737705600000',
  timestamp: 1737705600000
}
```

---

## 5. 前端实现指南

### 5.1 WebSocket管理器

```typescript
import {
  WebSocketMessage,
  WebSocketMessageType,
  MessageBuilder
} from './websocket';

class AgentWebSocketManager {
  private ws: WebSocket | null = null;
  private sessionCode: string;
  private token: string;
  private heartbeatTimer: number | null = null;

  // 事件回调
  private onPrompt?: (data: AgentPromptData) => void;
  private onStream?: (content: string) => void;
  private onComplete?: () => void;
  private onToolRequest?: (data: AgentToolCallRequestData) => Promise<void>;

  constructor(sessionCode: string, token: string) {
    this.sessionCode = sessionCode;
    this.token = token;
  }

  connect() {
    const url = `ws://localhost:8080/ws/chat/${this.sessionCode}?token=${this.token}`;
    this.ws = new WebSocket(url);

    this.ws.onopen = () => {
      console.log('WebSocket连接成功');
      this.startHeartbeat();
    };

    this.ws.onmessage = (event) => {
      this.handleMessage(JSON.parse(event.data));
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error);
    };

    this.ws.onclose = () => {
      console.log('WebSocket连接关闭');
      this.stopHeartbeat();
    };
  }

  private handleMessage(msg: WebSocketMessage) {
    switch (msg.type) {
      case WebSocketMessageType.AGENT_PROMPT:
        this.onPrompt?.(msg.data);
        break;

      case WebSocketMessageType.AGENT_STREAM:
        this.onStream?.(msg.content || '');
        break;

      case WebSocketMessageType.AGENT_COMPLETE:
        this.onComplete?.();
        break;

      case WebSocketMessageType.AGENT_TOOL_CALL_REQUEST:
        this.onToolRequest?.(msg.data);
        break;

      case WebSocketMessageType.PONG:
        console.log('收到心跳响应');
        break;
    }
  }

  sendMessage(content: string, workflowContent: string, toolSchemas?: any[]) {
    const msg = MessageBuilder.userMessage(
      this.sessionCode,
      content,
      workflowContent,
      toolSchemas
    );
    this.send(msg);
  }

  sendToolResponse(
    requestId: string,
    toolName: string,
    toolArgs: string,
    isClientTool: boolean,
    isAllow: boolean,
    result?: string,
    success?: boolean,
    error?: string
  ) {
    const msg = isAllow
      ? MessageBuilder.toolCallAllow(
          this.sessionCode, requestId, toolName,
          toolArgs, isClientTool, result, success, error
        )
      : MessageBuilder.toolCallDeny(
          this.sessionCode, requestId, toolName,
          toolArgs, isClientTool
        );
    this.send(msg);
  }

  interrupt(requestId: string) {
    const msg = MessageBuilder.interrupt(this.sessionCode, requestId);
    this.send(msg);
  }

  private send(msg: WebSocketMessage) {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(msg));
    }
  }

  private startHeartbeat() {
    this.heartbeatTimer = window.setInterval(() => {
      const msg = MessageBuilder.ping(this.sessionCode);
      this.send(msg);
    }, 30000); // 30秒
  }

  private stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  disconnect() {
    this.stopHeartbeat();
    this.ws?.close();
    this.ws = null;
  }

  // 注册事件回调
  on(event: 'prompt', handler: (data: AgentPromptData) => void): void;
  on(event: 'stream', handler: (content: string) => void): void;
  on(event: 'complete', handler: () => void): void;
  on(event: 'toolRequest', handler: (data: AgentToolCallRequestData) => Promise<void>): void;
  on(event: string, handler: any) {
    switch (event) {
      case 'prompt': this.onPrompt = handler; break;
      case 'stream': this.onStream = handler; break;
      case 'complete': this.onComplete = handler; break;
      case 'toolRequest': this.onToolRequest = handler; break;
    }
  }
}

export default AgentWebSocketManager;
```

---
