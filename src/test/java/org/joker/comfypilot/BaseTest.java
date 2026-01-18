package org.joker.comfypilot;

import org.joker.comfypilot.common.config.EnvLoader;
import org.junit.jupiter.api.BeforeAll;

/**
 * 纯单元测试基类
 * 所有纯单元测试类都应该继承此类，自动加载环境变量
 *
 * <p>此基类不依赖Spring容器，适用于纯单元测试场景</p>
 *
 * <h3>如何使用环境变量：</h3>
 * <pre>
 * // 1. 在测试方法中获取环境变量
 * String apiKey = System.getProperty("OPENAI_API_KEY");
 * String dbUrl = System.getProperty("DATABASE_URL");
 *
 * // 2. 提供默认值
 * String timeout = System.getProperty("REQUEST_TIMEOUT", "30");
 *
 * // 3. 类型转换
 * int maxRetries = Integer.parseInt(System.getProperty("MAX_RETRIES", "3"));
 * boolean debugMode = Boolean.parseBoolean(System.getProperty("DEBUG_MODE", "false"));
 * </pre>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>环境变量从 classpath 下的 .env 文件加载</li>
 *   <li>如果需要 Spring 容器支持，请使用 BaseIntegrationTest</li>
 *   <li>System.getProperty() 可能返回 null，建议提供默认值</li>
 * </ul>
 */
public abstract class BaseTest {

    /**
     * 在所有测试方法执行前加载环境变量
     * 使用 @BeforeAll 确保只加载一次
     */
    @BeforeAll
    public static void setUpBeforeClass() {
        EnvLoader.load();
    }
}
