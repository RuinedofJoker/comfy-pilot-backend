package org.joker.comfypilot.tool.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MCP 外部工具配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "mcp")
public class McpProperties {

    /**
     * 是否启用 MCP 工具加载
     */
    private Boolean enabled = true;

    /**
     * 是否只允许url方式的mcp tool
     */
    private Boolean onlyAllowUrlTool = true;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectionTimeout = 5000;

    /**
     * 工具调用超时时间（毫秒）
     */
    private Integer callTimeout = 30000;

    /**
     * 失败重试次数
     */
    private Integer retryAttempts = 3;

    /**
     * 连接失败是否阻止应用启动
     * true: 连接失败时抛出异常，阻止启动
     * false: 连接失败时记录日志，继续启动
     */
    private Boolean failFast = false;
}
