# TraceId 日志追踪系统使用说明

## 1. 系统概述

本项目已集成完整的 TraceId 日志追踪系统，用于在分布式环境下追踪请求的完整生命周期。

## 2. 核心组件

### 2.1 文件清单

```
src/main/resources/
└── logback-spring.xml                    # Logback 日志配置

src/main/java/org/joker/comfypilot/common/
├── util/
│   └── TraceIdUtil.java                  # TraceId 工具类
└── config/
    ├── TraceIdInterceptor.java           # HTTP 请求拦截器
    ├── WebMvcConfig.java                 # Web MVC 配置
    ├── TraceIdTaskDecorator.java         # 异步任务装饰器
    └── AsyncConfig.java                  # 异步线程池配置
```

### 2.2 功能特性

✅ **自动注入 TraceId**：所有 HTTP 请求自动生成或传递 TraceId
✅ **日志输出**：所有日志自动包含 TraceId
✅ **异步任务支持**：异步任务自动传递 TraceId
✅ **响应头返回**：TraceId 自动添加到响应头
✅ **多环境配置**：支持 dev/test/prod 环境

## 3. 使用方式

### 3.1 HTTP 请求中的 TraceId

#### 客户端传递 TraceId

客户端可以在请求头中传递 TraceId：

```http
GET /api/v1/workflows HTTP/1.1
Host: localhost:8080
X-Trace-Id: abc123def456
```

如果客户端不传递，系统会自动生成。

#### 服务端返回 TraceId

服务端会在响应头中返回 TraceId：

```http
HTTP/1.1 200 OK
X-Trace-Id: abc123def456
Content-Type: application/json
```

### 3.2 在代码中使用 TraceId

#### 获取当前 TraceId

```java
import org.joker.comfypilot.common.util.TraceIdUtil;

public class WorkflowService {

    public void processWorkflow() {
        String traceId = TraceIdUtil.getTraceId();
        log.info("Processing workflow with traceId: {}", traceId);
    }
}
```

#### 手动设置 TraceId（特殊场景）

```java
// 在非 HTTP 请求场景（如定时任务、消息队列消费）
TraceIdUtil.setTraceId(TraceIdUtil.generateTraceId());
try {
    // 业务逻辑
    doSomething();
} finally {
    // 清除 TraceId
    TraceIdUtil.clear();
}
```

### 3.3 异步任务中的 TraceId

使用 `@Async` 注解的异步方法会自动传递 TraceId：

```java
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class WorkflowAsyncService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowAsyncService.class);

    @Async("asyncExecutor")
    public void processAsync() {
        // TraceId 会自动传递到异步线程
        log.info("Async task executing");
    }
}
```

### 3.4 手动创建异步任务

如果需要手动创建线程或使用线程池，使用装饰器：

```java
import org.joker.comfypilot.common.config.TraceIdTaskDecorator;

// Runnable 任务
Runnable task = () -> {
    log.info("Task executing");
};
executor.execute(TraceIdTaskDecorator.decorate(task));

// Callable 任务
Callable<String> callable = () -> {
    log.info("Callable executing");
    return "result";
};
Future<String> future = executor.submit(TraceIdTaskDecorator.decorate(callable));
```

## 4. 日志配置说明

### 4.1 日志格式

```
2026-01-14 16:30:45.123 [http-nio-8080-exec-1] [abc123def456] INFO  o.j.c.w.WorkflowService - Processing workflow
```

格式说明：
- `2026-01-14 16:30:45.123`：时间戳
- `[http-nio-8080-exec-1]`：线程名
- `[abc123def456]`：TraceId（黄色高亮）
- `INFO`：日志级别
- `o.j.c.w.WorkflowService`：Logger 名称
- `Processing workflow`：日志内容

### 4.2 日志文件

| 文件名 | 说明 | 滚动策略 |
|--------|------|----------|
| `comfy-pilot-all.log` | 所有级别日志 | 100MB/文件，保留30天 |
| `comfy-pilot-error.log` | ERROR 级别日志 | 50MB/文件，保留60天 |

### 4.3 环境配置

在 `application.yml` 中指定环境：

```yaml
spring:
  profiles:
    active: dev  # dev/test/prod
```

不同环境的日志级别：
- **dev**：DEBUG 级别，输出到控制台 + 文件
- **test**：INFO 级别，输出到控制台 + 文件
- **prod**：WARN 级别，仅输出到文件

## 5. 最佳实践

### 5.1 日志记录规范

```java
// ✅ 推荐：使用占位符
log.info("User {} created workflow {}", userId, workflowId);

// ❌ 不推荐：字符串拼接
log.info("User " + userId + " created workflow " + workflowId);
```

### 5.2 异常日志记录

```java
try {
    // 业务逻辑
} catch (Exception e) {
    // ✅ 推荐：记录异常堆栈
    log.error("Failed to process workflow", e);

    // ❌ 不推荐：仅记录消息
    log.error("Failed to process workflow: " + e.getMessage());
}
```

### 5.3 TraceId 传递到外部系统

调用外部 API 时传递 TraceId：

```java
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

public void callExternalApi() {
    String traceId = TraceIdUtil.getTraceId();

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Trace-Id", traceId);

    // 使用 RestTemplate 或 WebClient 发送请求
    restTemplate.exchange(url, HttpMethod.GET,
        new HttpEntity<>(headers), String.class);
}
```

## 6. 故障排查

### 6.1 TraceId 为空

**问题**：日志中 TraceId 显示为空

**原因**：
1. 非 HTTP 请求场景（定时任务、消息队列）
2. 异步任务未使用装饰器

**解决**：
```java
// 手动设置 TraceId
TraceIdUtil.setTraceId(TraceIdUtil.generateTraceId());
```

### 6.2 异步任务 TraceId 丢失

**问题**：异步任务中 TraceId 为空

**原因**：
1. 未使用 `@Async("asyncExecutor")`
2. 手动创建线程未使用装饰器

**解决**：
```java
// 方案1：使用配置的线程池
@Async("asyncExecutor")
public void asyncMethod() { }

// 方案2：使用装饰器
executor.execute(TraceIdTaskDecorator.decorate(task));
```

### 6.3 日志文件未生成

**问题**：日志文件未在 `logs/` 目录生成

**原因**：
1. 目录权限不足
2. 磁盘空间不足

**解决**：
```bash
# 创建日志目录
mkdir logs
chmod 755 logs
```

## 7. 性能优化

### 7.1 异步日志

配置已启用异步日志（`AsyncAppender`），避免日志 I/O 阻塞业务线程。

### 7.2 日志级别

生产环境建议使用 `INFO` 或 `WARN` 级别，避免过多 `DEBUG` 日志影响性能。

### 7.3 日志采样

对于高频日志，可以使用采样：

```java
// 每100次请求记录一次
if (counter.incrementAndGet() % 100 == 0) {
    log.info("High frequency operation");
}
```

## 8. 扩展功能

### 8.1 集成 ELK

日志格式已支持 ELK（Elasticsearch + Logstash + Kibana）解析：

```json
{
  "timestamp": "2026-01-14T16:30:45.123",
  "thread": "http-nio-8080-exec-1",
  "traceId": "abc123def456",
  "level": "INFO",
  "logger": "org.joker.comfypilot.workflow.WorkflowService",
  "message": "Processing workflow"
}
```

### 8.2 集成 SkyWalking

如需集成 SkyWalking，修改 `TraceIdUtil`：

```java
// 从 SkyWalking 获取 TraceId
import org.apache.skywalking.apm.toolkit.trace.TraceContext;

public static String getTraceId() {
    String skyWalkingTraceId = TraceContext.traceId();
    return skyWalkingTraceId != null ? skyWalkingTraceId : MDC.get(TRACE_ID);
}
```

## 9. 常见问题

**Q: TraceId 会影响性能吗？**
A: 影响极小。MDC 基于 ThreadLocal，性能开销可忽略。

**Q: 日志文件会占用多少磁盘空间？**
A: 默认配置：all.log 最多 10GB，error.log 最多 5GB。

**Q: 如何在前端展示 TraceId？**
A: 从响应头 `X-Trace-Id` 获取，用于错误上报。

**Q: 支持分布式追踪吗？**
A: 当前支持单体应用内追踪。分布式追踪建议集成 SkyWalking 或 Zipkin。

---

**文档版本**：v1.0
**创建日期**：2026-01-14
**维护者**：开发团队
