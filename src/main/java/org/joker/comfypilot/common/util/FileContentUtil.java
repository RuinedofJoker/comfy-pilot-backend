package org.joker.comfypilot.common.util;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.BusinessException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * 文件内容工具类
 * <p>
 * 提供文件内容读取和 MIME 类型检测功能，支持本地文件和远程 URL。
 */
@Slf4j
public class FileContentUtil {

    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final int CONNECTION_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 30000; // 30秒

    /**
     * 私有构造函数，防止实例化
     */
    private FileContentUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 读取文件内容并转换为 Base64 编码
     * <p>
     * 支持本地文件路径和远程 URL。
     * <ul>
     *   <li>本地文件：支持绝对路径和相对路径</li>
     *   <li>远程 URL：支持 http:// 和 https:// 协议</li>
     * </ul>
     *
     * @param pathOrUrl 本地文件路径或远程 URL
     * @return Base64 编码的文件内容
     * @throws BusinessException 文件读取失败或文件过大时抛出
     */
    public static String toBase64(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            throw new BusinessException("文件路径或 URL 不能为空");
        }

        try {
            byte[] fileBytes;

            if (isUrl(pathOrUrl)) {
                // 从 URL 读取
                fileBytes = readFromUrl(pathOrUrl);
                log.debug("从 URL 读取文件成功: url={}, size= bytes", pathOrUrl, fileBytes.length);
            } else {
                // 从本地文件读取
                fileBytes = readFromLocalFile(pathOrUrl);
                log.debug("从本地文件读取成功: path={}, size={} bytes", pathOrUrl, fileBytes.length);
            }

            // 转换为 Base64
            return Base64.getEncoder().encodeToString(fileBytes);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("读取文件失败: pathOrUrl={}, error={}", pathOrUrl, e.getMessage(), e);
            throw new BusinessException("读取文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件大小（字节数）
     * <p>
     * 支持本地文件路径和远程 URL。
     * <ul>
     *   <li>本地文件：使用 Files.size() 获取</li>
     *   <li>远程 URL：从 HTTP 响应头的 Content-Length 获取</li>
     * </ul>
     *
     * @param pathOrUrl 本地文件路径或远程 URL
     * @return 文件大小（字节数）
     * @throws BusinessException 获取文件大小失败时抛出
     */
    public static long getFileSize(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            throw new BusinessException("文件路径或 URL 不能为空");
        }

        try {
            long fileSize;

            if (isUrl(pathOrUrl)) {
                // 从 URL 获取文件大小
                fileSize = getFileSizeFromUrl(pathOrUrl);
                log.debug("从 URL 获取文件大小: url={}, size={} bytes", pathOrUrl, fileSize);
            } else {
                // 从本地文件获取大小
                fileSize = getFileSizeFromLocalFile(pathOrUrl);
                log.debug("从本地文件获取大小: path={}, size={} bytes", pathOrUrl, fileSize);
            }

            return fileSize;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取文件大小失败: pathOrUrl={}, error={}", pathOrUrl, e.getMessage(), e);
            throw new BusinessException("获取文件大小失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件的 MIME 类型
     * <p>
     * 支持本地文件路径和远程 URL。
     * <ul>
     *   <li>本地文件：使用 Files.probeContentType() 检测</li>
     *   <li>远程 URL：从 HTTP 响应头的 Content-Type 获取</li>
     * </ul>
     *
     * @param pathOrUrl 本地文件路径或远程 URL
     * @return MIME 类型字符串，如 "image/png"、"application/pdf" 等
     * @throws BusinessException 获取 MIME 类型失败时抛出
     */
    public static String getMimeType(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.trim().isEmpty()) {
            throw new BusinessException("文件路径或 URL 不能为空");
        }

        try {
            String mimeType;

            if (isUrl(pathOrUrl)) {
                // 从 URL 获取 MIME 类型
                mimeType = getMimeTypeFromUrl(pathOrUrl);
                log.debug("从 URL 获取 MIME 类型: url={}, mimeType={}", pathOrUrl, mimeType);
            } else {
                // 从本地文件获取 MIME 类型
                mimeType = getMimeTypeFromLocalFile(pathOrUrl);
                log.debug("从本地文件获取 MIME 类型: path={}, mimeType={}", pathOrUrl, mimeType);
            }

            // 如果无法检测，返回默认值
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = "application/octet-stream";
                log.warn("无法检测 MIME 类型，使用默认值: pathOrUrl={}, mimeType={}", pathOrUrl, mimeType);
            }

            return mimeType;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取 MIME 类型失败: pathOrUrl={}, error={}", pathOrUrl, e.getMessage(), e);
            throw new BusinessException("获取 MIME 类型失败: " + e.getMessage());
        }
    }

    /**
     * 判断 MIME 类型是否为图片
     * <p>
     * 检查 MIME 类型是否以 "image/" 开头。
     *
     * @param mimeType MIME 类型字符串
     * @return true-是图片，false-不是图片
     */
    public static boolean isImage(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.toLowerCase().startsWith("image/");
    }

    /**
     * 判断 MIME 类型是否为视频
     * <p>
     * 检查 MIME 类型是否以 "video/" 开头。
     *
     * @param mimeType MIME 类型字符串
     * @return true-是视频，false-不是视频
     */
    public static boolean isVideo(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.toLowerCase().startsWith("video/");
    }

    /**
     * 判断 MIME 类型是否为音频
     * <p>
     * 检查 MIME 类型是否以 "audio/" 开头。
     *
     * @param mimeType MIME 类型字符串
     * @return true-是音频，false-不是音频
     */
    public static boolean isAudio(String mimeType) {
        if (mimeType == null || mimeType.isEmpty()) {
            return false;
        }
        return mimeType.toLowerCase().startsWith("audio/");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 判断字符串是否为 URL
     */
    private static boolean isUrl(String pathOrUrl) {
        if (pathOrUrl == null) {
            return false;
        }
        String lower = pathOrUrl.toLowerCase().trim();
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /**
     * 从本地文件读取字节数组
     */
    private static byte[] readFromLocalFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // 检查文件是否存在
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在: " + filePath);
        }

        // 检查是否为文件
        if (!Files.isRegularFile(path)) {
            throw new BusinessException("路径不是文件: " + filePath);
        }

        // 检查文件大小
        long fileSize = Files.size(path);
        if (fileSize > MAX_FILE_SIZE) {
            throw new BusinessException(
                    String.format("文件过大: %d bytes，最大支持 %d bytes", fileSize, MAX_FILE_SIZE)
            );
        }

        // 读取文件
        return Files.readAllBytes(path);
    }

    /**
     * 从 URL 读取字节数组
     */
    private static byte[] readFromUrl(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new BusinessException(
                        String.format("HTTP 请求失败: url=%s, responseCode=%d", urlString, responseCode)
                );
            }

            // 检查文件大小
            long contentLength = connection.getContentLengthLong();
            if (contentLength > MAX_FILE_SIZE) {
                throw new BusinessException(
                        String.format("文件过大: %d bytes，最大支持 %d bytes", contentLength, MAX_FILE_SIZE)
                );
            }

            // 读取内容
            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    totalBytesRead += bytesRead;

                    // 防止超大文件
                    if (totalBytesRead > MAX_FILE_SIZE) {
                        throw new BusinessException(
                                String.format("文件过大: 超过 %d bytes", MAX_FILE_SIZE)
                        );
                    }

                    outputStream.write(buffer, 0, bytesRead);
                }

                return outputStream.toByteArray();
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 从本地文件获取大小
     */
    private static long getFileSizeFromLocalFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // 检查文件是否存在
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在: " + filePath);
        }

        // 检查是否为文件
        if (!Files.isRegularFile(path)) {
            throw new BusinessException("路径不是文件: " + filePath);
        }

        // 获取文件大小
        return Files.size(path);
    }

    /**
     * 从本地文件获取 MIME 类型
     */
    private static String getMimeTypeFromLocalFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);

        // 检查文件是否存在
        if (!Files.exists(path)) {
            throw new BusinessException("文件不存在: " + filePath);
        }

        // 使用 Files.probeContentType() 检测
        String mimeType = Files.probeContentType(path);

        // 如果无法检测，尝试根据文件扩展名推断
        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = getMimeTypeByExtension(filePath);
        }

        return mimeType;
    }

    /**
     * 从 URL 获取文件大小
     */
    private static long getFileSizeFromUrl(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // 只获取头部信息
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new BusinessException(
                        String.format("HTTP 请求失败: url=%s, responseCode=%d", urlString, responseCode)
                );
            }

            // 从响应头获取 Content-Length
            long contentLength = connection.getContentLengthLong();

            // 如果无法获取 Content-Length（返回 -1）
            if (contentLength < 0) {
                throw new BusinessException("无法从 URL 获取文件大小（服务器未返回 Content-Length）");
            }

            return contentLength;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 从 URL 获取 MIME 类型
     */
    private static String getMimeTypeFromUrl(String urlString) throws IOException {
        URL url = URI.create(urlString).toURL();
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // 只获取头部信息
            connection.setConnectTimeout(CONNECTION_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new BusinessException(
                        String.format("HTTP 请求失败: url=%s, responseCode=%d", urlString, responseCode)
                );
            }

            // 从响应头获取 Content-Type
            String mimeType = connection.getContentType();

            // 移除可能的字符集信息（如 "text/html; charset=UTF-8"）
            if (mimeType != null && mimeType.contains(";")) {
                mimeType = mimeType.split(";")[0].trim();
            }

            // 如果无法获取，尝试根据 URL 扩展名推断
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = getMimeTypeByExtension(urlString);
            }

            return mimeType;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 根据文件扩展名推断 MIME 类型
     */
    private static String getMimeTypeByExtension(String pathOrUrl) {
        if (pathOrUrl == null || pathOrUrl.isEmpty()) {
            return null;
        }

        // 获取文件扩展名
        String extension = "";
        int lastDotIndex = pathOrUrl.lastIndexOf('.');
        int lastSlashIndex = Math.max(pathOrUrl.lastIndexOf('/'), pathOrUrl.lastIndexOf('\\'));

        if (lastDotIndex > lastSlashIndex && lastDotIndex < pathOrUrl.length() - 1) {
            extension = pathOrUrl.substring(lastDotIndex + 1).toLowerCase();
        }

        // 常见 MIME 类型映射
        return switch (extension) {
            // 图片
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "ico" -> "image/x-icon";

            // 视频
            case "mp4" -> "video/mp4";
            case "avi" -> "video/x-msvideo";
            case "mov" -> "video/quicktime";
            case "wmv" -> "video/x-ms-wmv";
            case "flv" -> "video/x-flv";
            case "webm" -> "video/webm";
            case "mkv" -> "video/x-matroska";

            // 音频
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";
            case "m4a" -> "audio/mp4";
            case "flac" -> "audio/flac";
            case "aac" -> "audio/aac";

            // 文档
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt" -> "text/plain";
            case "csv" -> "text/csv";

            // Web 相关
            case "html", "htm" -> "text/html";
            case "css" -> "text/css";
            case "js" -> "application/javascript";
            case "json" -> "application/json";
            case "xml" -> "application/xml";

            // 压缩文件
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            case "tar" -> "application/x-tar";
            case "gz" -> "application/gzip";

            // 其他
            case "bin" -> "application/octet-stream";
            default -> null;
        };
    }
}
