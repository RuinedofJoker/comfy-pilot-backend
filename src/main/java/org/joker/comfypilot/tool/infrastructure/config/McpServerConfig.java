package org.joker.comfypilot.tool.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * MCP 服务器配置
 * 对应 mcp.json 文件的数据结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpServerConfig {

    /**
     * 服务器名称
     */
    private String name;

    /**
     * 传输方式
     * 支持: "stdio", "streamable-http"
     */
    private String transport;

    /**
     * 命令（用于 stdio 传输）
     */
    private String command;

    /**
     * 命令参数（用于 stdio 传输）
     */
    private List<String> args;

    /**
     * 服务器 URL（用于 HTTP 传输）
     */
    private String url;

    /**
     * HTTP 请求头（用于 HTTP 传输）
     */
    private Map<String, String> headers;

    /**
     * 环境变量
     */
    private Map<String, String> env;
}
