package org.joker.comfypilot.tool.infrastructure.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.infrastructure.config.McpProperties;
import org.joker.comfypilot.tool.infrastructure.config.McpServerConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 配置加载器
 * <p>
 * 负责读取和解析 mcp.json 配置文件，连接外部 MCP Server，
 * 并获取服务器上注册的所有工具。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpConfigLoader {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final McpProperties mcpProperties;

    /**
     * 加载所有 MCP 外部工具
     *
     * @return MCP 工具列表
     */
    public List<Tool> loadMcpTools() {
        List<Tool> tools = new ArrayList<>();

        // 检查是否启用 MCP
        if (!mcpProperties.getEnabled()) {
            log.info("MCP 工具加载已禁用");
            return tools;
        }

        try {
            // 读取并解析配置文件
            String content = "";
            Map<String, McpServerConfig> serverConfigs = parseMcpConfig(content);

            log.info("解析到 {} 个 MCP 服务器配置", serverConfigs.size());

            // 遍历每个服务器配置
            for (Map.Entry<String, McpServerConfig> entry : serverConfigs.entrySet()) {
                String serverName = entry.getKey();
                McpServerConfig config = entry.getValue();
                config.setName(serverName);

                try {
                    List<Tool> serverTools = loadToolsFromServer(serverName, config);
                    tools.addAll(serverTools);
                    log.info("从 MCP 服务器 {} 加载了 {} 个工具", serverName, serverTools.size());
                } catch (Exception e) {
                    String errorMsg = String.format("加载 MCP 服务器 %s 的工具失败: %s",
                                                   serverName, e.getMessage());
                    log.error(errorMsg, e);

                    if (mcpProperties.getFailFast()) {
                        throw new BusinessException(errorMsg);
                    }
                }
            }

            log.info("MCP 工具加载完成，共加载 {} 个工具", tools.size());
            return tools;

        } catch (Exception e) {
            String errorMsg = "加载 MCP 配置文件失败: " + e.getMessage();
            log.error(errorMsg, e);

            if (mcpProperties.getFailFast()) {
                throw new BusinessException(errorMsg);
            }
            return tools;
        }
    }

    /**
     * 解析 MCP 配置文件
     *
     * @param content 配置文件内容
     * @return 服务器配置映射表
     */
    private Map<String, McpServerConfig> parseMcpConfig(String content) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(content);
        JsonNode mcpServers = root.get("mcpServers");

        if (mcpServers == null || !mcpServers.isObject()) {
            log.warn("配置文件中未找到 mcpServers 节点");
            return new HashMap<>();
        }

        Map<String, McpServerConfig> configs = new HashMap<>();
        mcpServers.fields().forEachRemaining(entry -> {
            String serverName = entry.getKey();
            JsonNode serverNode = entry.getValue();

            try {
                McpServerConfig config = OBJECT_MAPPER.treeToValue(serverNode, McpServerConfig.class);
                configs.put(serverName, config);
            } catch (Exception e) {
                log.error("解析服务器配置失败: serverName={}, error={}", serverName, e.getMessage(), e);
            }
        });

        return configs;
    }

    /**
     * 从 MCP 服务器加载工具
     *
     * @param serverName 服务器名称
     * @param config     服务器配置
     * @return 工具列表
     */
    private List<Tool> loadToolsFromServer(String serverName, McpServerConfig config) throws Exception {
        List<Tool> tools = new ArrayList<>();

        // 目前仅支持 streamable-http 传输方式
        if (!"streamable-http".equalsIgnoreCase(config.getTransport())) {
            log.warn("暂不支持的传输方式: serverName={}, transport={}", 
                    serverName, config.getTransport());
            return tools;
        }

        // 检查 URL
        if (config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            throw new BusinessException("MCP 服务器 URL 未配置: " + serverName);
        }

        log.info("连接 MCP 服务器: serverName={}, url={}", serverName, config.getUrl());

        // 创建 WebClient
        WebClient webClient = createWebClient(config);

        // 获取服务器上的工具列表
        List<McpSchema.Tool> toolSchemas = fetchToolsFromServer(webClient, serverName);

        log.info("从服务器 {} 获取到 {} 个工具", serverName, toolSchemas.size());

        // 为每个工具创建 McpServerTool
        for (McpSchema.Tool toolSchema : toolSchemas) {
            try {
                McpServerTool tool = new McpServerTool(
                        serverName,
                        toolSchema,
                        webClient,
                        mcpProperties.getCallTimeout(),
                        mcpProperties.getRetryAttempts()
                );
                tools.add(tool);
            } catch (Exception e) {
                log.error("创建 MCP 工具失败: serverName={}, toolName={}, error={}",
                         serverName, toolSchema.name(), e.getMessage(), e);
            }
        }

        return tools;
    }

    /**
     * 创建 WebClient
     *
     * @param config 服务器配置
     * @return WebClient 实例
     */
    private WebClient createWebClient(McpServerConfig config) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(config.getUrl());

        // 添加自定义请求头
        if (config.getHeaders() != null && !config.getHeaders().isEmpty()) {
            builder.defaultHeaders(headers -> {
                config.getHeaders().forEach(headers::add);
            });
        }

        return builder.build();
    }

    /**
     * 从服务器获取工具列表
     *
     * @param webClient  WebClient 实例
     * @param serverName 服务器名称
     * @return 工具 Schema 列表
     */
    private List<McpSchema.Tool> fetchToolsFromServer(WebClient webClient, String serverName) throws Exception {
        try {
            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                "method", "tools/list",
                "params", Map.of()
            );

            // 发送请求
            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(java.time.Duration.ofMillis(mcpProperties.getConnectionTimeout()))
                    .block();

            // 解析响应
            JsonNode root = OBJECT_MAPPER.readTree(response);
            JsonNode toolsNode = root.get("result").get("tools");

            if (toolsNode == null || !toolsNode.isArray()) {
                log.warn("服务器响应中未找到工具列表: serverName={}", serverName);
                return new ArrayList<>();
            }

            // 转换为 McpSchema.Tool 列表
            List<McpSchema.Tool> tools = new ArrayList<>();
            for (JsonNode toolNode : toolsNode) {
                try {
                    McpSchema.Tool tool = OBJECT_MAPPER.treeToValue(toolNode, McpSchema.Tool.class);
                    tools.add(tool);
                } catch (Exception e) {
                    log.error("解析工具 Schema 失败: serverName={}, error={}", 
                             serverName, e.getMessage(), e);
                }
            }

            return tools;

        } catch (Exception e) {
            log.error("从服务器获取工具列表失败: serverName={}, error={}", 
                     serverName, e.getMessage(), e);
            throw e;
        }
    }
}
