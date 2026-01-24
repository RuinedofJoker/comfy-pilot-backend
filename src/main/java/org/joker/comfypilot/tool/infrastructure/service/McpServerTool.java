package org.joker.comfypilot.tool.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

/**
 * MCP 外部服务器工具实现类
 * <p>
 * 封装对外部 MCP Server 的工具调用，通过 Streamable HTTP 协议与远程服务器通信。
 * <p>
 * <b>核心功能：</b>
 * <ul>
 *   <li>将 MCP 工具元数据转换为 LangChain4j ToolSpecification</li>
 *   <li>通过 HTTP 调用远程 MCP Server 的工具</li>
 *   <li>支持超时、重试机制</li>
 *   <li>完善的异常处理和日志记录</li>
 * </ul>
 */
@Slf4j
@EqualsAndHashCode(callSuper = false)
public class McpServerTool implements Tool {

    /**
     * JSON 序列化/反序列化工具
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 工具名称（带前缀）
     */
    private final String toolName;

    /**
     * 原始工具名称（不带前缀）
     */
    private final String originalToolName;

    /**
     * 服务器名称
     */
    private final String serverName;

    /**
     * MCP 工具规范
     */
    private final McpSchema.Tool toolSchema;

    /**
     * LangChain4j 工具规范（转换后）
     */
    private final ToolSpecification toolSpecification;

    /**
     * WebClient 用于 HTTP 调用
     */
    private final WebClient webClient;

    /**
     * 调用超时时间（毫秒）
     */
    private final int callTimeout;

    /**
     * 重试次数
     */
    private final int retryAttempts;

    /**
     * 构造 MCP 外部服务器工具
     *
     * @param serverName    服务器名称
     * @param toolSchema    MCP 工具规范
     * @param webClient     WebClient 实例
     * @param callTimeout   调用超时时间（毫秒）
     * @param retryAttempts 重试次数
     */
    public McpServerTool(String serverName,
                         McpSchema.Tool toolSchema,
                         WebClient webClient,
                         int callTimeout,
                         int retryAttempts) {
        this.serverName = serverName;
        this.toolSchema = toolSchema;
        this.originalToolName = toolSchema.name();
        this.toolName = Tool.SERVER_TOOL_PREFIX + "mcp_" + serverName + "_" + originalToolName;
        this.webClient = webClient;
        this.callTimeout = callTimeout;
        this.retryAttempts = retryAttempts;
        this.toolSpecification = convertToToolSpecification(toolSchema);

        log.debug("创建 MCP 外部工具: serverName={}, toolName={}, originalName={}",
                serverName, toolName, originalToolName);
    }

    @Override
    public String toolName() {
        return toolName;
    }

    @Override
    public ToolSpecification toolSpecification() {
        return toolSpecification;
    }

    /**
     * 执行工具
     * <p>
     * 通过 HTTP 调用远程 MCP Server 的工具。
     *
     * @param name      工具名称（用于日志记录）
     * @param arguments JSON 格式的参数字符串
     * @return JSON 格式的执行结果
     * @throws BusinessException 工具执行失败时抛出
     */
    @Override
    public String executeTool(String toolCallId, String name, String arguments) {
        try {
            log.debug("开始执行 MCP 外部工具: serverName={}, toolName={}, toolCallId={}, arguments={}",
                    serverName, originalToolName, toolCallId, arguments);

            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                    "method", "tools/call",
                    "params", Map.of(
                            "name", originalToolName,
                            "arguments", OBJECT_MAPPER.readValue(arguments, Map.class)
                    )
            );

            // 发送 HTTP 请求
            String result = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(callTimeout))
                    .retryWhen(Retry.fixedDelay(retryAttempts, Duration.ofSeconds(1))
                            .doBeforeRetry(signal -> log.warn("重试 MCP 工具调用: serverName={}, toolName={}, attempt={}",
                                    serverName, originalToolName, signal.totalRetries() + 1)))
                    .block();

            log.debug("MCP 工具执行成功: serverName={}, toolName={}, result={}",
                    serverName, originalToolName, result);

            return result;

        } catch (Exception e) {
            log.error("MCP 工具执行失败: serverName={}, toolName={}, arguments={}, error={}",
                    serverName, originalToolName, arguments, e.getMessage(), e);
            throw new BusinessException("MCP 工具执行失败: " + e.getMessage());
        }
    }

    /**
     * 将 MCP 工具规范转换为 LangChain4j 工具规范
     *
     * @param mcpTool MCP 工具规范
     * @return LangChain4j 工具规范
     * @throws BusinessException 转换失败时抛出
     */
    private ToolSpecification convertToToolSpecification(McpSchema.Tool mcpTool) {
        try {
            String name = mcpTool.name();
            String description = mcpTool.description();
            JsonObjectSchema parameters = convertInputSchema(mcpTool.inputSchema());

            return ToolSpecification.builder()
                    .name(toolName)
                    .description(description)
                    .parameters(parameters)
                    .metadata(mcpTool.meta())
                    .build();

        } catch (Exception e) {
            log.error("转换 MCP 工具规范失败: toolName={}, error={}",
                    mcpTool.name(), e.getMessage(), e);
            throw new BusinessException("转换 MCP 工具规范失败: " + e.getMessage());
        }
    }

    /**
     * 转换 MCP 输入 Schema 为 LangChain4j JsonObjectSchema
     *
     * @param inputSchema MCP 输入 Schema
     * @return LangChain4j JsonObjectSchema
     */
    private JsonObjectSchema convertInputSchema(McpSchema.JsonSchema inputSchema) {
        if (inputSchema == null) {
            return JsonObjectSchema.builder().build();
        }
        return OBJECT_MAPPER.convertValue(inputSchema, JsonObjectSchema.class);
    }
}
