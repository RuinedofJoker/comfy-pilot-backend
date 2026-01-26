package org.joker.comfypilot.script.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 脚本运行时配置
 * 用于配置 Python、Node.js 等外部脚本运行时环境
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "script")
public class ScriptRuntimeConfig {

    /**
     * Python 配置
     */
    private PythonConfig python = new PythonConfig();

    /**
     * Node.js 配置（预留，未来扩展）
     */
    private NodeConfig node = new NodeConfig();

    /**
     * Python 运行时配置
     */
    @Data
    public static class PythonConfig {
        /**
         * 是否启用 Python 支持
         */
        private boolean enabled = true;

        /**
         * Python 可执行文件路径
         * 可以是相对路径（从 PATH 查找）或绝对路径
         * 示例：
         * - "python3" (Linux/Mac)
         * - "python" (Windows)
         * - "/usr/local/bin/python3" (绝对路径)
         */
        private String executable = detectDefaultExecutable();

        /**
         * 是否在启动时检查版本
         */
        private boolean versionCheck = true;

        /**
         * 最低版本要求
         */
        private String minVersion = "3.8";

        /**
         * 命令执行超时时间（毫秒）
         */
        private int timeout = 5000;

        /**
         * 脚本执行超时时间（秒）
         */
        private int scriptTimeout = 30;

        /**
         * 自动检测默认的 Python 可执行文件名
         */
        private static String detectDefaultExecutable() {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                return "python";  // Windows 通常使用 python
            } else {
                return "python3"; // Linux/Mac 使用 python3
            }
        }
    }

    /**
     * Node.js 运行时配置（预留）
     */
    @Data
    public static class NodeConfig {
        /**
         * 是否启用 Node.js 支持
         */
        private boolean enabled = false;

        /**
         * Node.js 可执行文件路径
         */
        private String executable = "node";

        /**
         * 是否在启动时检查版本
         */
        private boolean versionCheck = true;

        /**
         * 最低版本要求
         */
        private String minVersion = "14.0";

        /**
         * 命令执行超时时间（毫秒）
         */
        private int timeout = 5000;
    }
}
