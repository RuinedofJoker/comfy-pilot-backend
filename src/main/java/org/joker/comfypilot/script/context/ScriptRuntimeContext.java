package org.joker.comfypilot.script.context;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 脚本运行时上下文
 * 提供静态方法访问 Python/Node.js 运行时信息
 */
@Slf4j
public class ScriptRuntimeContext {

    // ==================== Python 运行时信息 ====================

    /**
     * Python 是否可用
     * -- GETTER --
     *  Python 是否可用

     */
    @Getter
    private static volatile boolean pythonAvailable = false;

    /**
     * Python 版本号
     * -- GETTER --
     *  获取 Python 版本号

     */
    @Getter
    private static volatile String pythonVersion = null;

    /**
     * Python 可执行文件路径
     * -- GETTER --
     *  获取 Python 可执行文件路径

     */
    @Getter
    private static volatile String pythonExecutable = null;

    /**
     * Python 包安装路径
     * -- GETTER --
     *  获取 Python 包安装路径

     */
    @Getter
    private static volatile List<String> pythonSitePackages = null;

    // ==================== Node.js 运行时信息（预留） ====================

    /**
     * Node.js 是否可用
     * -- GETTER --
     *  Node.js 是否可用

     */
    @Getter
    private static volatile boolean nodeAvailable = false;

    /**
     * Node.js 版本号
     * -- GETTER --
     *  获取 Node.js 版本号

     */
    @Getter
    private static volatile String nodeVersion = null;

    /**
     * Node.js 可执行文件路径
     * -- GETTER --
     *  获取 Node.js 可执行文件路径

     */
    @Getter
    private static volatile String nodeExecutable = null;

    // ==================== Python 相关方法 ====================

    /**
     * 设置 Python 运行时信息
     *
     * @param available      是否可用
     * @param version        版本号
     * @param executable     可执行文件路径
     * @param sitePackages   包安装路径
     */
    public static void setPythonRuntime(boolean available, String version, String executable, List<String> sitePackages) {
        pythonAvailable = available;
        pythonVersion = version;
        pythonExecutable = executable;
        pythonSitePackages = sitePackages;
        log.debug("Python 运行时信息已更新: available={}, version={}, executable={}",
                available, version, executable);
    }

    /**
     * 检查 Python 是否可用，如果不可用则抛出异常
     *
     * @throws IllegalStateException 如果 Python 不可用
     */
    public static void requirePython() {
        if (!pythonAvailable) {
            throw new IllegalStateException("Python 运行时不可用，请检查配置或安装 Python");
        }
    }

    // ==================== Node.js 相关方法（预留） ====================

    /**
     * 设置 Node.js 运行时信息
     *
     * @param available  是否可用
     * @param version    版本号
     * @param executable 可执行文件路径
     */
    public static void setNodeRuntime(boolean available, String version, String executable) {
        nodeAvailable = available;
        nodeVersion = version;
        nodeExecutable = executable;
        log.debug("Node.js 运行时信息已更新: available={}, version={}, executable={}",
                available, version, executable);
    }

    /**
     * 检查 Node.js 是否可用，如果不可用则抛出异常
     *
     * @throws IllegalStateException 如果 Node.js 不可用
     */
    public static void requireNode() {
        if (!nodeAvailable) {
            throw new IllegalStateException("Node.js 运行时不可用，请检查配置或安装 Node.js");
        }
    }

    // ==================== 通用方法 ====================

    /**
     * 重置所有运行时信息（主要用于测试）
     */
    public static void reset() {
        pythonAvailable = false;
        pythonVersion = null;
        pythonExecutable = null;
        pythonSitePackages = null;
        nodeAvailable = false;
        nodeVersion = null;
        nodeExecutable = null;
        log.debug("脚本运行时上下文已重置");
    }

    /**
     * 获取运行时状态摘要
     */
    public static String getStatusSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("脚本运行时状态:\n");
        sb.append("  Python: ").append(pythonAvailable ? "✓ 可用" : "✗ 不可用");
        if (pythonAvailable) {
            sb.append(" (版本: ").append(pythonVersion).append(")");
        }
        sb.append("\n");
        sb.append("  Node.js: ").append(nodeAvailable ? "✓ 可用" : "✗ 不可用");
        if (nodeAvailable) {
            sb.append(" (版本: ").append(nodeVersion).append(")");
        }
        return sb.toString();
    }

    // 私有构造函数，防止实例化
    private ScriptRuntimeContext() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
