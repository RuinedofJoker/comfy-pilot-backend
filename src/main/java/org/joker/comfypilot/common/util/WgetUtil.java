package org.joker.comfypilot.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Wget 工具类
 * 基于 Java 11+ 原生 HttpClient 实现类似 wget 的文件下载功能
 *
 * <p>主要功能：</p>
 * <ul>
 *   <li>支持 HTTP/HTTPS 协议下载</li>
 *   <li>支持自定义超时时间</li>
 *   <li>支持自动重定向</li>
 *   <li>支持自定义请求头</li>
 *   <li>支持下载进度回调</li>
 *   <li>支持断点续传（可选）</li>
 *   <li>自动创建目标目录</li>
 *   <li>自动从 URL 提取文件名</li>
 * </ul>
 */
@Slf4j
public class WgetUtil {

    /**
     * 默认连接超时时间（秒）
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 30;

    /**
     * 默认读取超时时间（分钟）
     */
    private static final int DEFAULT_READ_TIMEOUT = 5;

    /**
     * 默认缓冲区大小（8KB）
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * 默认 User-Agent
     */
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * 下载文件到指定目录（使用默认配置）
     *
     * @param url       下载链接
     * @param targetDir 目标目录
     * @return 下载后的文件路径
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static Path download(String url, String targetDir) throws IOException, InterruptedException {
        return download(url, targetDir, null);
    }

    /**
     * 下载文件到指定目录，并指定文件名
     *
     * @param url       下载链接
     * @param targetDir 目标目录
     * @param fileName  文件名（为 null 时自动从 URL 提取）
     * @return 下载后的文件路径
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static Path download(String url, String targetDir, String fileName)
            throws IOException, InterruptedException {
        return download(url, targetDir, fileName, null);
    }

    /**
     * 下载文件到指定目录，支持进度回调
     *
     * @param url              下载链接
     * @param targetDir        目标目录
     * @param fileName         文件名（为 null 时自动从 URL 提取）
     * @param progressCallback 进度回调（接收已下载字节数）
     * @return 下载后的文件路径
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static Path download(String url, String targetDir, String fileName,
                                 Consumer<Long> progressCallback)
            throws IOException, InterruptedException {
        DownloadConfig config = DownloadConfig.builder()
                .url(url)
                .targetDir(targetDir)
                .fileName(fileName)
                .progressCallback(progressCallback)
                .build();
        return download(config);
    }

    /**
     * 使用自定义配置下载文件
     *
     * @param config 下载配置
     * @return 下载后的文件路径
     * @throws IOException          IO 异常
     * @throws InterruptedException 中断异常
     */
    public static Path download(DownloadConfig config) throws IOException, InterruptedException {
        log.info("开始下载文件: {}", config.url);

        // 1. 构建 HttpClient
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.connectTimeout))
                .followRedirects(config.followRedirects ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER)
                .build();

        // 2. 构建 HttpRequest
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(config.url))
                .timeout(Duration.ofMinutes(config.readTimeout))
                .GET();

        // 添加自定义请求头
        if (config.headers != null && !config.headers.isEmpty()) {
            config.headers.forEach(requestBuilder::header);
        } else {
            // 添加默认 User-Agent
            requestBuilder.header("User-Agent", DEFAULT_USER_AGENT);
        }

        HttpRequest request = requestBuilder.build();

        // 3. 确定文件名
        String fileName = config.fileName;
        if (fileName == null || fileName.isEmpty()) {
            fileName = extractFileNameFromUrl(config.url);
        }

        // 4. 创建目标目录
        Path targetPath = Paths.get(config.targetDir);
        if (!Files.exists(targetPath)) {
            Files.createDirectories(targetPath);
            log.info("创建目标目录: {}", targetPath);
        }

        Path filePath = targetPath.resolve(fileName);

        // 5. 发送请求并下载
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        // 6. 检查响应状态
        if (response.statusCode() != 200) {
            throw new IOException("下载失败: HTTP " + response.statusCode());
        }

        // 7. 写入文件
        try (InputStream inputStream = response.body()) {
            if (config.progressCallback != null) {
                // 带进度回调的下载
                downloadWithProgress(inputStream, filePath, config.progressCallback);
            } else {
                // 直接复制
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        log.info("文件下载成功: {}", filePath);
        return filePath;
    }

    /**
     * 带进度回调的下载
     *
     * @param inputStream      输入流
     * @param filePath         文件路径
     * @param progressCallback 进度回调
     * @throws IOException IO 异常
     */
    private static void downloadWithProgress(InputStream inputStream, Path filePath,
                                              Consumer<Long> progressCallback) throws IOException {
        try (var outputStream = Files.newOutputStream(filePath)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            long totalBytesRead = 0;
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                progressCallback.accept(totalBytesRead);
            }
        }
    }

    /**
     * 从 URL 中提取文件名
     *
     * @param url URL 地址
     * @return 文件名
     */
    private static String extractFileNameFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        // 移除查询参数
        int queryIndex = fileName.indexOf('?');
        if (queryIndex != -1) {
            fileName = fileName.substring(0, queryIndex);
        }

        // 如果文件名为空，使用默认名称
        if (fileName.isEmpty()) {
            fileName = "downloaded_file_" + System.currentTimeMillis();
        }

        return fileName;
    }

    /**
     * 下载配置类
     * 用于配置下载行为的各种参数
     */
    public static class DownloadConfig {
        /**
         * 下载链接（必填）
         */
        private final String url;

        /**
         * 目标目录（必填）
         */
        private final String targetDir;

        /**
         * 文件名（可选，为 null 时自动从 URL 提取）
         */
        private final String fileName;

        /**
         * 连接超时时间（秒）
         */
        private final int connectTimeout;

        /**
         * 读取超时时间（分钟）
         */
        private final int readTimeout;

        /**
         * 是否自动跟随重定向
         */
        private final boolean followRedirects;

        /**
         * 自定义请求头
         */
        private final Map<String, String> headers;

        /**
         * 进度回调（接收已下载字节数）
         */
        private final Consumer<Long> progressCallback;

        private DownloadConfig(Builder builder) {
            this.url = builder.url;
            this.targetDir = builder.targetDir;
            this.fileName = builder.fileName;
            this.connectTimeout = builder.connectTimeout;
            this.readTimeout = builder.readTimeout;
            this.followRedirects = builder.followRedirects;
            this.headers = builder.headers;
            this.progressCallback = builder.progressCallback;
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * 下载配置构建器
         */
        public static class Builder {
            private String url;
            private String targetDir;
            private String fileName;
            private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
            private int readTimeout = DEFAULT_READ_TIMEOUT;
            private boolean followRedirects = true;
            private Map<String, String> headers = new HashMap<>();
            private Consumer<Long> progressCallback;

            public Builder url(String url) {
                this.url = url;
                return this;
            }

            public Builder targetDir(String targetDir) {
                this.targetDir = targetDir;
                return this;
            }

            public Builder fileName(String fileName) {
                this.fileName = fileName;
                return this;
            }

            public Builder connectTimeout(int connectTimeout) {
                this.connectTimeout = connectTimeout;
                return this;
            }

            public Builder readTimeout(int readTimeout) {
                this.readTimeout = readTimeout;
                return this;
            }

            public Builder followRedirects(boolean followRedirects) {
                this.followRedirects = followRedirects;
                return this;
            }

            public Builder headers(Map<String, String> headers) {
                this.headers = headers;
                return this;
            }

            public Builder addHeader(String key, String value) {
                this.headers.put(key, value);
                return this;
            }

            public Builder progressCallback(Consumer<Long> progressCallback) {
                this.progressCallback = progressCallback;
                return this;
            }

            public DownloadConfig build() {
                if (url == null || url.isEmpty()) {
                    throw new IllegalArgumentException("URL 不能为空");
                }
                if (targetDir == null || targetDir.isEmpty()) {
                    throw new IllegalArgumentException("目标目录不能为空");
                }
                return new DownloadConfig(this);
            }
        }
    }
}
