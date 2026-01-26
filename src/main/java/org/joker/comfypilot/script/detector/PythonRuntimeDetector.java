package org.joker.comfypilot.script.detector;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.script.config.ScriptRuntimeConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Python 运行时检测器
 * 在应用启动时检测 Python 环境是否可用
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PythonRuntimeDetector implements CommandLineRunner {

    private final ScriptRuntimeConfig config;

    /**
     * Python 是否可用
     */
    @Getter
    private volatile boolean pythonAvailable = false;

    /**
     * Python 版本号
     */
    @Getter
    private volatile String pythonVersion = null;

    /**
     * Python 可执行文件的实际路径
     */
    @Getter
    private volatile String pythonExecutable = null;

    /**
     * Python 包安装路径
     */
    @Getter
    private volatile List<String> pythonSitePackages = null;

    /**
     * 版本号提取正则
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("Python\\s+(\\d+\\.\\d+\\.\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    public void run(String... args) {
        if (!config.getPython().isEnabled()) {
            log.info("Python 支持已禁用（配置: script.python.enabled=false）");
            return;
        }

        log.info("开始检测 Python 运行时环境...");
        detectPython();
    }

    /**
     * 检测 Python 环境
     */
    private void detectPython() {
        ScriptRuntimeConfig.PythonConfig pythonConfig = config.getPython();

        // 尝试多个可能的 Python 路径
        List<String> candidates = buildCandidatePaths(pythonConfig.getExecutable());

        for (String candidate : candidates) {
            if (testPythonExecutable(candidate)) {
                pythonExecutable = candidate;
                pythonAvailable = true;

                // 获取包安装路径
                pythonSitePackages = getPythonSitePackages(candidate);

                log.info("✓ Python 环境检测成功");
                log.info("  - 可执行文件: {}", pythonExecutable);
                log.info("  - 版本: {}", pythonVersion);
                log.info("  - 最低要求: {}", pythonConfig.getMinVersion());

                if (pythonSitePackages != null && !pythonSitePackages.isEmpty()) {
                    log.info("  - 包安装路径:");
                    for (String path : pythonSitePackages) {
                        log.info("    • {}", path);
                    }
                }

                return;
            }
        }

        // 所有路径都失败
        log.warn("✗ Python 环境不可用");
        log.warn("  - 已尝试的路径: {}", candidates);
        log.warn("  - 提示: 如需使用 Python 相关功能，请:");
        log.warn("    1. 安装 Python {} 或更高版本", pythonConfig.getMinVersion());
        log.warn("    2. 确保 Python 已添加到系统 PATH 环境变量");
        log.warn("    3. 或在配置文件中指定 Python 可执行文件的绝对路径");
        log.warn("       示例: script.python.executable=/usr/local/bin/python3");
    }

    /**
     * 测试指定的 Python 可执行文件是否可用
     */
    private boolean testPythonExecutable(String executable) {
        try {
            log.debug("测试 Python 可执行文件: {}", executable);

            ProcessBuilder pb = new ProcessBuilder(executable, "--version");
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 等待进程完成，带超时
            boolean finished = process.waitFor(
                    config.getPython().getTimeout(),
                    TimeUnit.MILLISECONDS
            );

            if (!finished) {
                process.destroyForcibly();
                log.debug("  ✗ 超时");
                return false;
            }

            if (process.exitValue() != 0) {
                log.debug("  ✗ 退出码: {}", process.exitValue());
                return false;
            }

            // 读取输出
            String output = readProcessOutput(process);
            log.debug("  输出: {}", output);

            // 提取版本号
            String version = extractVersion(output);
            if (version == null) {
                log.debug("  ✗ 无法解析版本号");
                return false;
            }

            // 检查版本要求
            if (config.getPython().isVersionCheck()) {
                if (!checkVersionRequirement(version, config.getPython().getMinVersion())) {
                    log.debug("  ✗ 版本过低: {} (要求: {})", version, config.getPython().getMinVersion());
                    return false;
                }
            }

            // 检测成功
            this.pythonVersion = version;
            log.debug("  ✓ 版本: {}", version);
            return true;

        } catch (Exception e) {
            log.debug("  ✗ 异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 读取进程输出
     */
    private String readProcessOutput(Process process) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString().trim();
        }
    }

    /**
     * 从输出中提取版本号
     * 示例输入: "Python 3.10.5"
     * 示例输出: "3.10.5"
     */
    private String extractVersion(String output) {
        Matcher matcher = VERSION_PATTERN.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 检查版本是否满足要求
     */
    private boolean checkVersionRequirement(String actualVersion, String requiredVersion) {
        try {
            return compareVersion(actualVersion, requiredVersion) >= 0;
        } catch (Exception e) {
            log.warn("版本比较失败: actual={}, required={}", actualVersion, requiredVersion, e);
            return false;
        }
    }

    /**
     * 比较两个版本号
     * 返回值: >0 表示 v1 > v2, =0 表示相等, <0 表示 v1 < v2
     */
    private int compareVersion(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return num1 - num2;
            }
        }

        return 0;
    }

    /**
     * 构建候选 Python 路径列表
     */
    private List<String> buildCandidatePaths(String configuredPath) {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            // Windows 候选路径
            return List.of(
                    configuredPath,
                    "python",
                    "python3",
                    "C:\\Python310\\python.exe",
                    "C:\\Python311\\python.exe",
                    "C:\\Python312\\python.exe"
            );
        } else {
            // Linux/Mac 候选路径
            return List.of(
                    configuredPath,
                    "python3",
                    "python",
                    "/usr/bin/python3",
                    "/usr/local/bin/python3",
                    "/opt/homebrew/bin/python3"
            );
        }
    }

    /**
     * 获取 Python 包安装路径
     * 执行: python -c "import site; print(site.getsitepackages())"
     */
    private List<String> getPythonSitePackages(String executable) {
        try {
            log.debug("获取 Python 包安装路径...");

            ProcessBuilder pb = new ProcessBuilder(
                    executable,
                    "-c",
                    "import site; import json; print(json.dumps(site.getsitepackages()))"
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // 等待进程完成，带超时
            boolean finished = process.waitFor(
                    config.getPython().getTimeout(),
                    TimeUnit.MILLISECONDS
            );

            if (!finished) {
                process.destroyForcibly();
                log.debug("  ✗ 获取包路径超时");
                return null;
            }

            if (process.exitValue() != 0) {
                log.debug("  ✗ 获取包路径失败，退出码: {}", process.exitValue());
                return null;
            }

            // 读取输出
            String output = readProcessOutput(process);
            log.debug("  包路径输出: {}", output);

            // 解析 JSON 数组
            return parseSitePackages(output);

        } catch (Exception e) {
            log.debug("  ✗ 获取包路径异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析 site.getsitepackages() 的 JSON 输出
     * 示例输入: ["C:\\Python310\\Lib\\site-packages"]
     */
    private List<String> parseSitePackages(String jsonOutput) {
        try {
            // 简单的 JSON 数组解析（避免引入 Jackson 依赖）
            String trimmed = jsonOutput.trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
                return null;
            }

            // 移除首尾的 [ ]
            String content = trimmed.substring(1, trimmed.length() - 1);
            if (content.trim().isEmpty()) {
                return List.of();
            }

            // 分割并清理引号
            String[] paths = content.split(",");
            return java.util.Arrays.stream(paths)
                    .map(String::trim)
                    .map(s -> s.replaceAll("^\"|\"$", "")) // 移除首尾引号
                    .map(s -> s.replace("\\\\", "\\")) // 处理转义的反斜杠
                    .toList();

        } catch (Exception e) {
            log.debug("  ✗ 解析包路径 JSON 失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取 Python 环境状态信息（供健康检查使用）
     */
    public PythonStatus getStatus() {
        return new PythonStatus(
                pythonAvailable,
                pythonVersion,
                pythonExecutable,
                config.getPython().getMinVersion()
        );
    }

    /**
     * Python 状态信息
     */
    public record PythonStatus(
            boolean available,
            String version,
            String executable,
            String minVersion
    ) {
    }
}
