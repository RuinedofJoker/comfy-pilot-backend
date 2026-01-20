package org.joker.comfypilot.common.config;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 环境变量加载器
 * 从 classpath 下的 .env 文件加载环境变量到系统属性中
 */
@Slf4j
public class EnvLoader {

    private static final String ENV_FILE = ".env";

    /**
     * 加载环境变量
     */
    public static void load() {
        try (InputStream inputStream = EnvLoader.class.getClassLoader().getResourceAsStream(ENV_FILE)) {
            if (inputStream == null) {
                log.warn("未找到 .env 文件，跳过环境变量加载");
                return;
            }

            loadFromInputStream(inputStream);
            log.info("环境变量加载完成");

        } catch (IOException e) {
            log.error("加载环境变量失败", e);
            throw new BusinessException("加载环境变量失败", e);
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
