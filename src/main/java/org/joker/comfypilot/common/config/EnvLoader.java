package org.joker.comfypilot.common.config;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 环境变量加载器
 * 优先从 classpath 下的 .env 文件加载环境变量，如果找不到则从启动目录查找
 */
@Slf4j
public class EnvLoader {

    private static final String ENV_FILE = ".env";
    private static final String ENV_FILE_PROPERTY = "env.file.location";

    /**
     * 加载环境变量
     * 优先级：系统属性指定路径 > classpath > 工作目录 > JAR包同级目录
     */
    public static void load() {
        try {
            // 1. 尝试从系统属性指定的路径加载（最高优先级）
            if (loadFromSystemProperty()) {
                log.info("从系统属性指定路径加载环境变量完成");
                return;
            }

            // 2. 尝试从 classpath 加载
            if (loadFromClasspath()) {
                log.info("从 classpath 加载环境变量完成");
                return;
            }

            // 3. 尝试从工作目录加载
            if (loadFromWorkingDirectory()) {
                log.info("从工作目录加载环境变量完成");
                return;
            }

            // 4. 尝试从 JAR 包同级目录加载
            if (loadFromJarDirectory()) {
                log.info("从 JAR 包同级目录加载环境变量完成");
                return;
            }

            // 5. 都找不到，记录警告
            log.warn("未找到 .env 文件（已尝试系统属性、classpath、工作目录和 JAR 包目录），跳过环境变量加载");

        } catch (IOException e) {
            log.error("加载环境变量失败", e);
            throw new BusinessException("加载环境变量失败", e);
        }
    }

    /**
     * 从系统属性指定的路径加载 .env 文件
     * 使用方式：java -Denv.file.location=/path/to/.env -jar app.jar
     * @return 是否成功加载
     */
    private static boolean loadFromSystemProperty() throws IOException {
        String envFilePath = System.getProperty(ENV_FILE_PROPERTY);

        if (envFilePath == null || envFilePath.trim().isEmpty()) {
            log.debug("未指定系统属性 {}", ENV_FILE_PROPERTY);
            return false;
        }

        Path envPath = Paths.get(envFilePath.trim());

        if (!Files.exists(envPath)) {
            log.warn("系统属性指定的 .env 文件不存在: {}", envPath);
            return false;
        }

        if (!Files.isRegularFile(envPath)) {
            log.warn("系统属性指定的路径不是文件: {}", envPath);
            return false;
        }

        log.info("从系统属性指定路径加载 .env 文件: {}", envPath);
        try (InputStream inputStream = Files.newInputStream(envPath)) {
            loadFromInputStream(inputStream);
            return true;
        }
    }

    /**
     * 从 classpath 加载 .env 文件
     * @return 是否成功加载
     */
    private static boolean loadFromClasspath() throws IOException {
        try (InputStream inputStream = EnvLoader.class.getClassLoader().getResourceAsStream(ENV_FILE)) {
            if (inputStream == null) {
                log.debug("classpath 中未找到 .env 文件");
                return false;
            }

            log.info("从 classpath 加载 .env 文件");
            loadFromInputStream(inputStream);
            return true;
        }
    }

    /**
     * 从工作目录加载 .env 文件
     * @return 是否成功加载
     */
    private static boolean loadFromWorkingDirectory() throws IOException {
        Path envPath = Paths.get(System.getProperty("user.dir"), ENV_FILE);

        if (!Files.exists(envPath)) {
            log.debug("工作目录中未找到 .env 文件: {}", envPath);
            return false;
        }

        log.info("从工作目录加载 .env 文件: {}", envPath);
        try (InputStream inputStream = Files.newInputStream(envPath)) {
            loadFromInputStream(inputStream);
            return true;
        }
    }

    /**
     * 从 JAR 包同级目录加载 .env 文件
     * @return 是否成功加载
     */
    private static boolean loadFromJarDirectory() throws IOException {
        try {
            // 获取当前类所在的 JAR 包路径
            String jarPath = EnvLoader.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            Path jarDir;
            if (jarPath.endsWith(".jar")) {
                // 运行在 JAR 包中，获取 JAR 包所在目录
                jarDir = Paths.get(jarPath).getParent();
            } else {
                // 运行在 IDE 中（classes 目录），跳过此方式
                log.debug("非 JAR 包运行环境，跳过 JAR 包目录查找");
                return false;
            }

            if (jarDir == null) {
                log.debug("无法确定 JAR 包目录");
                return false;
            }

            Path envPath = jarDir.resolve(ENV_FILE);
            if (!Files.exists(envPath)) {
                log.debug("JAR 包目录中未找到 .env 文件: {}", envPath);
                return false;
            }

            log.info("从 JAR 包目录加载 .env 文件: {}", envPath);
            try (InputStream inputStream = Files.newInputStream(envPath)) {
                loadFromInputStream(inputStream);
                return true;
            }

        } catch (Exception e) {
            log.debug("从 JAR 包目录加载失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从输入流加载环境变量
     */
    private static void loadFromInputStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                processLine(line, lineNumber);
            }
        }
    }

    /**
     * 处理单行配置
     */
    private static void processLine(String line, int lineNumber) {
        // 去除首尾空格
        line = line.trim();

        // 跳过空行和注释行
        if (line.isEmpty() || line.startsWith("#")) {
            return;
        }

        // 解析键值对
        int separatorIndex = line.indexOf('=');
        if (separatorIndex == -1) {
            log.warn("第 {} 行格式错误，跳过: ", lineNumber, line);
            return;
        }

        String key = line.substring(0, separatorIndex).trim();
        String value = line.substring(separatorIndex + 1).trim();

        if (key.isEmpty()) {
            log.warn("第 {} 行键为空，跳过: {}", lineNumber, line);
            return;
        }

        // 设置系统属性
        System.setProperty(key, value);
        log.debug("加载环境变量: {} = {}", key, maskSensitiveValue(key, value));
    }

    /**
     * 对敏感信息进行脱敏处理
     */
    private static String maskSensitiveValue(String key, String value) {
        // 对密码等敏感信息进行脱敏
        if (key.toUpperCase().contains("PASSWORD") ||
            key.toUpperCase().contains("SECRET") ||
            key.toUpperCase().contains("TOKEN")) {
            return "******";
        }
        return value;
    }
}
