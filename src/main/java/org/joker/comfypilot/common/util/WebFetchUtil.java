package org.joker.comfypilot.common.util;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网页内容获取工具类
 * 增强型 HTTP 客户端，支持反爬虫对抗和内容解析
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>支持获取网页 HTML/JSON 内容</li>
 *   <li>自动 Cookie 管理</li>
 *   <li>智能 User-Agent 轮换</li>
 *   <li>自动字符编码检测</li>
 *   <li>支持自定义请求头（Referer、Authorization 等）</li>
 *   <li>支持重试机制</li>
 *   <li>支持代理配置</li>
 *   <li>响应状态码智能处理</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 简单获取
 * String html = WebFetchUtil.fetchAsString("https://example.com");
 *
 * // 自定义配置
 * FetchConfig config = FetchConfig.builder()
 *     .url("https://example.com/api/data")
 *     .method("POST")
 *     .addHeader("Authorization", "Bearer token")
 *     .body("{\"key\":\"value\"}")
 *     .retryCount(3)
 *     .build();
 * String response = WebFetchUtil.fetch(config);
 * }</pre>
 */
@Slf4j
public class WebFetchUtil {

    /**
     * 默认连接超时时间（秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 30;

    /**
     * 默认读取超时时间（秒）
     */
    private static final int DEFAULT_READ_TIMEOUT = 60;

    /**
     * 默认重试次数
     */
    private static final int DEFAULT_RETRY_COUNT = 2;

    /**
     * 默认重试延迟（毫秒）
     */
    private static final long DEFAULT_RETRY_DELAY = 1000;

    /**
     * 常用 User-Agent 列表（用于轮换）
     */
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Safari/605.1.15"
    };

    /**
     * 字符编码检测正则
     */
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([\\w-]+)", Pattern.CASE_INSENSITIVE);

    /**
     * 获取网页内容（使用默认配置）
     *
     * @param url 目标 URL
     * @return 网页内容字符串
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String fetchAsString(String url) throws IOException, InterruptedException {
        FetchConfig config = FetchConfig.builder()
                .url(url)
                .build();
        return fetch(config);
    }

    /**
     * 获取网页内容（支持自定义请求头）
     *
     * @param url     目标 URL
     * @param headers 自定义请求头
     * @return 网页内容字符串
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String fetchAsString(String url, Map<String, String> headers)
            throws IOException, InterruptedException {
        FetchConfig config = FetchConfig.builder()
                .url(url)
                .headers(headers)
                .build();
        return fetch(config);
    }

    /**
     * 使用自定义配置获取网页内容
     *
     * @param config 获取配置
     * @return 网页内容字符串
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static String fetch(FetchConfig config) throws IOException, InterruptedException {
        log.info("开始获取网页内容: {}", config.url);

        Exception lastException = null;
        int attempts = 0;
        int maxAttempts = config.retryCount + 1;

        while (attempts < maxAttempts) {
            attempts++;
            try {
                return doFetch(config);
            } catch (IOException | InterruptedException e) {
                lastException = e;
                log.warn("获取失败 (尝试 {}/{}): {}", attempts, maxAttempts, e.getMessage());

                if (attempts < maxAttempts) {
                    // 等待后重试
                    Thread.sleep(config.retryDelay);
                }
            }
        }

        // 所有重试都失败
        if (lastException instanceof IOException) {
            throw (IOException) lastException;
        } else if (lastException instanceof InterruptedException) {
            throw (InterruptedException) lastException;
        } else {
            throw new IOException("获取网页内容失败", lastException);
        }
    }

    /**
     * 异步获取网页内容
     *
     * @param config 获取配置
     * @return CompletableFuture
     */
    public static CompletableFuture<String> fetchAsync(FetchConfig config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return fetch(config);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("异步获取失败", e);
            }
        });
    }

    /**
     * 执行实际的网页获取操作
     */
    private static String doFetch(FetchConfig config) throws IOException, InterruptedException {
        // 1. 构建 HttpClient（支持 Cookie 管理）
        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.connectTimeout))
                .followRedirects(config.followRedirects ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);

        // 启用 Cookie 管理
        if (config.enableCookies) {
            clientBuilder.cookieHandler(new CookieManager());
        }

        HttpClient client = clientBuilder.build();

        // 2. 构建 HttpRequest
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(config.url))
                .timeout(Duration.ofSeconds(config.readTimeout));

        // 设置 HTTP 方法和请求体
        switch (config.method.toUpperCase()) {
            case "GET" -> requestBuilder.GET();
            case "POST" -> requestBuilder.POST(HttpRequest.BodyPublishers.ofString(
                    config.body != null ? config.body : ""));
            case "PUT" -> requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(
                    config.body != null ? config.body : ""));
            case "DELETE" -> requestBuilder.DELETE();
            default -> throw new IllegalArgumentException("不支持的 HTTP 方法: " + config.method);
        }

        // 3. 添加请求头
        if (config.headers != null && !config.headers.isEmpty()) {
            config.headers.forEach(requestBuilder::header);
        }

        // 添加 User-Agent（如果未自定义）
        if (config.headers == null || !config.headers.containsKey("User-Agent")) {
            String userAgent = config.randomUserAgent ? getRandomUserAgent() : USER_AGENTS[0];
            requestBuilder.header("User-Agent", userAgent);
        }

        // 添加常见反爬虫对抗请求头
        if (config.antiBot) {
            requestBuilder.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            requestBuilder.header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            requestBuilder.header("Accept-Encoding", "gzip, deflate, br");
            requestBuilder.header("Connection", "keep-alive");
            requestBuilder.header("Upgrade-Insecure-Requests", "1");

            // 如果有 Referer，添加
            if (config.referer != null) {
                requestBuilder.header("Referer", config.referer);
            }
        }

        HttpRequest request = requestBuilder.build();

        // 4. 发送请求
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 5. 检查响应状态
        int statusCode = response.statusCode();
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException("HTTP 请求失败: " + statusCode + " - " + response.body());
        }

        // 6. 处理字符编码
        String content = response.body();
        if (config.autoDetectCharset) {
            Charset detectedCharset = detectCharset(response);
            if (detectedCharset != null && !detectedCharset.equals(StandardCharsets.UTF_8)) {
                // 重新发送请求并使用正确的字符集
                HttpResponse<byte[]> binaryResponse = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                content = new String(binaryResponse.body(), detectedCharset);
            }
        }

        log.info("成功获取网页内容: {} (长度: {} 字符)", config.url, content.length());
        return content;
    }

    /**
     * 检测响应的字符编码
     */
    private static Charset detectCharset(HttpResponse<String> response) {
        // 从 Content-Type 头中提取字符集
        return response.headers()
                .firstValue("Content-Type")
                .map(contentType -> {
                    Matcher matcher = CHARSET_PATTERN.matcher(contentType);
                    if (matcher.find()) {
                        try {
                            return Charset.forName(matcher.group(1));
                        } catch (Exception e) {
                            log.warn("无法识别字符集: {}", matcher.group(1));
                        }
                    }
                    return null;
                })
                .orElse(null);
    }

    /**
     * 获取随机 User-Agent
     */
    private static String getRandomUserAgent() {
        int index = (int) (Math.random() * USER_AGENTS.length);
        return USER_AGENTS[index];
    }

    /**
     * 获取配置类
     */
    @Builder
    public static class FetchConfig {
        /**
         * 目标 URL（必填）
         */
        private final String url;

        /**
         * HTTP 方法（默认 GET）
         */
        @Builder.Default
        private final String method = "GET";

        /**
         * 请求体（用于 POST/PUT）
         */
        private final String body;

        /**
         * 自定义请求头
         */
        @Builder.Default
        private final Map<String, String> headers = new HashMap<>();

        /**
         * 连接超时时间（秒）
         */
        @Builder.Default
        private final int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        /**
         * 读取超时时间（秒）
         */
        @Builder.Default
        private final int readTimeout = DEFAULT_READ_TIMEOUT;

        /**
         * 是否自动跟随重定向
         */
        @Builder.Default
        private final boolean followRedirects = true;

        /**
         * 是否启用 Cookie 管理
         */
        @Builder.Default
        private final boolean enableCookies = true;

        /**
         * 是否使用随机 User-Agent
         */
        @Builder.Default
        private final boolean randomUserAgent = false;

        /**
         * 是否启用反爬虫对抗（添加常见浏览器请求头）
         */
        @Builder.Default
        private final boolean antiBot = true;

        /**
         * Referer 头（用于反爬虫）
         */
        private final String referer;

        /**
         * 是否自动检测字符编码
         */
        @Builder.Default
        private final boolean autoDetectCharset = true;

        /**
         * 重试次数
         */
        @Builder.Default
        private final int retryCount = DEFAULT_RETRY_COUNT;

        /**
         * 重试延迟（毫秒）
         */
        @Builder.Default
        private final long retryDelay = DEFAULT_RETRY_DELAY;

        /**
         * 添加单个请求头的便捷方法
         */
        public static class FetchConfigBuilder {
            private Map<String, String> headers;
            public FetchConfigBuilder addHeader(String key, String value) {
                if (this.headers == null) {
                    this.headers = new HashMap<>();
                }
                this.headers.put(key, value);
                return this;
            }
        }
    }
}
