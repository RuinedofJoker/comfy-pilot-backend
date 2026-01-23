# WebSocket 使用示例

## 1. React 组件集成示例

### 1.1 基础聊天组件

```typescript
import React, { useEffect, useState, useRef } from 'react';
import AgentWebSocketManager from './AgentWebSocketManager';
import { AgentPromptType, AGENT_PROMPT_DEFAULT_MESSAGES } from './websocket';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

export const ChatComponent: React.FC = () => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [statusText, setStatusText] = useState('');
  const [currentRequestId, setCurrentRequestId] = useState('');

  const wsManager = useRef<AgentWebSocketManager | null>(null);
  const streamingContent = useRef('');

  useEffect(() => {
    // 初始化WebSocket
    const sessionCode = 'YOUR_SESSION_CODE';
    const token = 'YOUR_TOKEN';

    wsManager.current = new AgentWebSocketManager(sessionCode, token);

    // 注册事件处理器
    wsManager.current.on('prompt', (data) => {
      const message = data.message || AGENT_PROMPT_DEFAULT_MESSAGES[data.promptType];
      setStatusText(message);

      if (data.promptType === AgentPromptType.ERROR) {
        setIsLoading(false);
      }
    });

    wsManager.current.on('stream', (content) => {
      streamingContent.current += content;
      // 实时更新最后一条消息
      setMessages(prev => {
        const newMessages = [...prev];
        if (newMessages[newMessages.length - 1]?.role === 'assistant') {
          newMessages[newMessages.length - 1].content = streamingContent.current;
        } else {
          newMessages.push({ role: 'assistant', content: streamingContent.current });
        }
        return newMessages;
      });
    });

    wsManager.current.on('complete', () => {
      setIsLoading(false);
      setStatusText('');
      streamingContent.current = '';
    });

    wsManager.current.on('toolRequest', async (data) => {
      if (data.isClientTool) {
        // 执行客户端工具
        try {
          const result = await executeClientTool(data.toolName, data.toolArgs);
          wsManager.current?.sendToolResponse(
            currentRequestId,
            data.toolName,
            data.toolArgs,
            true,
            true,
            result,
            true
          );
        } catch (error) {
          wsManager.current?.sendToolResponse(
            currentRequestId,
            data.toolName,
            data.toolArgs,
            true,
            true,
            undefined,
            false,
            error.message
          );
        }
      } else {
        // 服务端工具：请求用户确认
        const allow = await confirmToolExecution(data.toolName, data.toolArgs);
        wsManager.current?.sendToolResponse(
          currentRequestId,
          data.toolName,
          data.toolArgs,
          false,
          allow
        );
      }
    });

    wsManager.current.connect();

    return () => {
      wsManager.current?.disconnect();
    };
  }, []);

  const handleSend = () => {
    if (!input.trim() || isLoading) return;

    const userMessage: Message = { role: 'user', content: input };
    setMessages(prev => [...prev, userMessage]);

    const requestId = Date.now().toString();
    setCurrentRequestId(requestId);
    setIsLoading(true);
    streamingContent.current = '';

    wsManager.current?.sendMessage(
      input,
      '{}', // workflowContent
      []    // toolSchemas
    );

    setInput('');
  };

  const handleStop = () => {
    wsManager.current?.interrupt(currentRequestId);
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg, idx) => (
          <div key={idx} className={`message ${msg.role}`}>
            {msg.content}
          </div>
        ))}
        {isLoading && statusText && (
          <div className="status-indicator">{statusText}</div>
        )}
      </div>

      <div className="input-area">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSend()}
          disabled={isLoading}
          placeholder="输入消息..."
        />
        {isLoading ? (
          <button onClick={handleStop}>停止</button>
        ) : (
          <button onClick={handleSend}>发送</button>
        )}
      </div>
    </div>
  );
};

// 工具执行函数示例
async function executeClientTool(toolName: string, toolArgs: string): Promise<string> {
  const args = JSON.parse(toolArgs);

  switch (toolName) {
    case 'readFile':
      // 实现文件读取逻辑
      return JSON.stringify({ content: 'file content' });

    case 'writeFile':
      // 实现文件写入逻辑
      return JSON.stringify({ success: true });

    default:
      throw new Error(`Unknown tool: ${toolName}`);
  }
}

// 工具确认对话框
async function confirmToolExecution(toolName: string, toolArgs: string): Promise<boolean> {
  return window.confirm(`是否允许执行工具 ${toolName}?\n参数: ${toolArgs}`);
}
```

---

## 2. Vue 组件集成示例

```vue
<template>
  <div class="chat-container">
    <div class="messages">
      <div
        v-for="(msg, idx) in messages"
        :key="idx"
        :class="['message', msg.role]"
      >
        {{ msg.content }}
      </div>
      <div v-if="isLoading && statusText" class="status-indicator">
        {{ statusText }}
      </div>
    </div>

    <div class="input-area">
      <input
        v-model="input"
        @keypress.enter="handleSend"
        :disabled="isLoading"
        placeholder="输入消息..."
      />
      <button v-if="isLoading" @click="handleStop">停止</button>
      <button v-else @click="handleSend">发送</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import AgentWebSocketManager from './AgentWebSocketManager';
import { AgentPromptType, AGENT_PROMPT_DEFAULT_MESSAGES } from './websocket';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

const messages = ref<Message[]>([]);
const input = ref('');
const isLoading = ref(false);
const statusText = ref('');
const currentRequestId = ref('');

let wsManager: AgentWebSocketManager | null = null;
let streamingContent = '';

onMounted(() => {
  const sessionCode = 'YOUR_SESSION_CODE';
  const token = 'YOUR_TOKEN';

  wsManager = new AgentWebSocketManager(sessionCode, token);

  wsManager.on('prompt', (data) => {
    const message = data.message || AGENT_PROMPT_DEFAULT_MESSAGES[data.promptType];
    statusText.value = message;

    if (data.promptType === AgentPromptType.ERROR) {
      isLoading.value = false;
    }
  });

  wsManager.on('stream', (content) => {
    streamingContent += content;
    const lastMsg = messages.value[messages.value.length - 1];
    if (lastMsg?.role === 'assistant') {
      lastMsg.content = streamingContent;
    } else {
      messages.value.push({ role: 'assistant', content: streamingContent });
    }
  });

  wsManager.on('complete', () => {
    isLoading.value = false;
    statusText.value = '';
    streamingContent = '';
  });

  wsManager.on('toolRequest', async (data) => {
    if (data.isClientTool) {
      try {
        const result = await executeClientTool(data.toolName, data.toolArgs);
        wsManager?.sendToolResponse(
          currentRequestId.value,
          data.toolName,
          data.toolArgs,
          true,
          true,
          result,
          true
        );
      } catch (error: any) {
        wsManager?.sendToolResponse(
          currentRequestId.value,
          data.toolName,
          data.toolArgs,
          true,
          true,
          undefined,
          false,
          error.message
        );
      }
    } else {
      const allow = await confirmToolExecution(data.toolName, data.toolArgs);
      wsManager?.sendToolResponse(
        currentRequestId.value,
        data.toolName,
        data.toolArgs,
        false,
        allow
      );
    }
  });

  wsManager.connect();
});

onUnmounted(() => {
  wsManager?.disconnect();
});

const handleSend = () => {
  if (!input.value.trim() || isLoading.value) return;

  messages.value.push({ role: 'user', content: input.value });

  currentRequestId.value = Date.now().toString();
  isLoading.value = true;
  streamingContent = '';

  wsManager?.sendMessage(input.value, '{}', []);
  input.value = '';
};

const handleStop = () => {
  wsManager?.interrupt(currentRequestId.value);
};

async function executeClientTool(toolName: string, toolArgs: string): Promise<string> {
  // 实现工具执行逻辑
  return JSON.stringify({ success: true });
}

async function confirmToolExecution(toolName: string, toolArgs: string): Promise<boolean> {
  return window.confirm(`是否允许执行工具 ${toolName}?\n参数: ${toolArgs}`);
}
</script>
```

---

## 3. 工具调用完整示例

### 3.1 客户端工具注册

```typescript
// tools.ts
export interface ClientTool {
  name: string;
  description: string;
  execute: (args: any) => Promise<any>;
}

export const clientTools: ClientTool[] = [
  {
    name: 'readFile',
    description: '读取文件内容',
    execute: async (args: { path: string }) => {
      // 使用浏览器File API或Electron API
      const content = await readFileFromSystem(args.path);
      return { content };
    }
  },
  {
    name: 'writeFile',
    description: '写入文件',
    execute: async (args: { path: string; content: string }) => {
      await writeFileToSystem(args.path, args.content);
      return { success: true };
    }
  },
  {
    name: 'listFiles',
    description: '列出目录文件',
    execute: async (args: { path: string }) => {
      const files = await listFilesInDirectory(args.path);
      return { files };
    }
  }
];

// 工具执行器
export async function executeClientTool(
  toolName: string,
  toolArgs: string
): Promise<string> {
  const tool = clientTools.find(t => t.name === toolName);
  if (!tool) {
    throw new Error(`Unknown tool: ${toolName}`);
  }

  const args = JSON.parse(toolArgs);
  const result = await tool.execute(args);
  return JSON.stringify(result);
}
```

### 3.2 MCP工具Schema生成

```typescript
// mcpTools.ts
import { McpToolSchema } from './websocket';
import { clientTools } from './tools';

export function generateMcpSchemas(): McpToolSchema[] {
  return clientTools.map(tool => ({
    name: tool.name,
    description: tool.description,
    inputSchema: {
      type: 'object',
      properties: getToolProperties(tool.name),
      required: getToolRequired(tool.name)
    }
  }));
}

function getToolProperties(toolName: string): Record<string, any> {
  switch (toolName) {
    case 'readFile':
      return {
        path: { type: 'string', description: '文件路径' }
      };
    case 'writeFile':
      return {
        path: { type: 'string', description: '文件路径' },
        content: { type: 'string', description: '文件内容' }
      };
    case 'listFiles':
      return {
        path: { type: 'string', description: '目录路径' }
      };
    default:
      return {};
  }
}

function getToolRequired(toolName: string): string[] {
  switch (toolName) {
    case 'readFile':
      return ['path'];
    case 'writeFile':
      return ['path', 'content'];
    case 'listFiles':
      return ['path'];
    default:
      return [];
  }
}
```

---

## 4. 错误处理最佳实践

```typescript
class RobustWebSocketManager extends AgentWebSocketManager {
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  connect() {
    try {
      super.connect();

      this.ws!.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.handleError('连接错误');
      };

      this.ws!.onclose = (event) => {
        console.log('WebSocket关闭:', event.code, event.reason);

        if (event.code !== 1000) { // 非正常关闭
          this.attemptReconnect();
        }
      };
    } catch (error) {
      this.handleError('连接失败');
    }
  }

  private attemptReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      this.handleError('重连失败，已达最大尝试次数');
      return;
    }

    this.reconnectAttempts++;
    const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);

    console.log(`${delay}ms后尝试第${this.reconnectAttempts}次重连...`);

    setTimeout(() => {
      this.connect();
    }, delay);
  }

  private handleError(message: string) {
    // 通知UI显示错误
    this.onError?.(message);
  }

  // 添加错误回调
  private onError?: (message: string) => void;

  onErrorOccurred(handler: (message: string) => void) {
    this.onError = handler;
  }
}
```

---

## 5. 性能优化建议

### 5.1 消息批处理

```typescript
class OptimizedWebSocketManager extends AgentWebSocketManager {
  private streamBuffer: string[] = [];
  private flushTimer: number | null = null;

  protected handleMessage(msg: WebSocketMessage) {
    if (msg.type === WebSocketMessageType.AGENT_STREAM) {
      // 批量处理流式消息
      this.streamBuffer.push(msg.content || '');
      this.scheduleFlush();
    } else {
      super.handleMessage(msg);
    }
  }

  private scheduleFlush() {
    if (this.flushTimer) return;

    this.flushTimer = window.setTimeout(() => {
      const content = this.streamBuffer.join('');
      this.streamBuffer = [];
      this.flushTimer = null;
      this.onStream?.(content);
    }, 16); // 60fps
  }
}
```

### 5.2 消息队列

```typescript
class QueuedWebSocketManager extends AgentWebSocketManager {
  private messageQueue: WebSocketMessage[] = [];
  private isSending = false;

  protected send(msg: WebSocketMessage) {
    this.messageQueue.push(msg);
    this.processQueue();
  }

  private async processQueue() {
    if (this.isSending || this.messageQueue.length === 0) return;

    this.isSending = true;

    while (this.messageQueue.length > 0) {
      const msg = this.messageQueue.shift()!;

      if (this.ws?.readyState === WebSocket.OPEN) {
        this.ws.send(JSON.stringify(msg));
        await this.delay(10); // 避免发送过快
      }
    }

    this.isSending = false;
  }

  private delay(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
```
