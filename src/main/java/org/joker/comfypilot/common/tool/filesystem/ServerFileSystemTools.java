package org.joker.comfypilot.common.tool.filesystem;

import cn.hutool.core.collection.ConcurrentHashSet;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * 本地文件系统操作服务
 * 提供安全的本地文件系统访问功能，类似于 MCP filesystem server
 * <p>
 * 安全限制：
 * - createDirectory: 只能在系统临时目录下创建随机名称的目录
 * - writeFile/delete/move: 只能操作临时目录下的文件
 * <p>
 * 资源清理：
 * - 应用关闭时自动清理所有创建的临时目录
 */
@Slf4j
@Component
@ToolSet
public class ServerFileSystemTools {

    /**
     * 线程安全的临时目录集合，用于跟踪所有创建的临时目录
     */
    private static final Set<String> createdTempDirectories = new ConcurrentHashSet<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("应用关闭，开始清理临时目录...");
            int successCount = 0;
            int failCount = 0;

            for (String dirPath : new ArrayList<>(createdTempDirectories)) {
                try {
                    Path path = Paths.get(dirPath);
                    if (Files.exists(path)) {
                        Files.walkFileTree(path, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        successCount++;
                        log.debug("临时目录清理成功: {}", dirPath);
                    }
                } catch (Exception e) {
                    failCount++;
                    log.warn("临时目录清理失败: {}, 原因: {}", dirPath, e.getMessage());
                }
            }

            log.info("临时目录清理完成，成功: {}, 失败: {}", successCount, failCount);
            createdTempDirectories.clear();
        }, "TempDirectoryCleanupThread"));
    }

    /**
     * 校验路径是否在临时目录下
     *
     * @param path 要校验的路径
     * @throws SecurityException 如果路径不在临时目录下
     */
    private void validatePathInTempDir(String path) {
        try {
            Path filePath = Paths.get(path).toRealPath();
            Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir")).toRealPath();

            if (!filePath.startsWith(tempDirPath)) {
                throw new SecurityException("安全限制：只能操作临时目录下的文件。临时目录: " + System.getProperty("java.io.tmpdir"));
            }
        } catch (IOException e) {
            // 如果路径不存在，检查其父目录
            try {
                Path filePath = Paths.get(path).toAbsolutePath().normalize();
                Path tempDirPath = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();

                if (!filePath.startsWith(tempDirPath)) {
                    throw new SecurityException("安全限制：只能操作临时目录下的文件。临时目录: " + System.getProperty("java.io.tmpdir"));
                }
            } catch (Exception ex) {
                throw new SecurityException("路径校验失败: " + path, ex);
            }
        }
    }

    /**
     * 检查路径是否存在
     *
     * @param path 文件或目录路径
     * @return 是否存在
     */
    @Tool(name = "exists", value = "Agent服务器上检查指定路径的文件或目录是否存在")
    public boolean exists(@P(value = "文件或目录的完整路径", required = true) String path) {
        // 安全校验：必须在临时目录下
        validatePathInTempDir(path);
        try {
            Path filePath = Paths.get(path);
            return Files.exists(filePath);
        } catch (Exception e) {
            log.error("检查路径是否存在失败: {}", path, e);
            return false;
        }
    }

    /**
     * 检查是否为目录
     *
     * @param path 路径
     * @return 是否为目录
     */
    @Tool(name = "isDirectory", value = "Agent服务器上检查指定路径是否为目录")
    public boolean isDirectory(@P(value = "要检查的路径", required = true) String path) {
        // 安全校验：必须在临时目录下
        validatePathInTempDir(path);
        try {
            Path filePath = Paths.get(path);
            return Files.isDirectory(filePath);
        } catch (Exception e) {
            log.error("检查是否为目录失败: {}", path, e);
            return false;
        }
    }

    /**
     * 检查是否为文件
     *
     * @param path 路径
     * @return 是否为文件
     */
    @Tool(name = "isFile", value = "Agent服务器上检查指定路径是否为普通文件")
    public boolean isFile(@P(value = "要检查的路径", required = true) String path) {
        // 安全校验：必须在临时目录下
        validatePathInTempDir(path);
        try {
            Path filePath = Paths.get(path);
            return Files.isRegularFile(filePath);
        } catch (Exception e) {
            log.error("检查是否为文件失败: {}", path, e);
            return false;
        }
    }

    /**
     * 读取文件内容
     *
     * @param path 文件路径
     * @return 文件内容
     * @throws IOException IO 异常
     */
    @Tool(name = "readFile", value = "Agent服务器上读取指定文件的文本内容")
    public String readFile(@P(value = "要读取的文件路径", required = true) String path) throws IOException {
        // 安全校验：必须在临时目录下
        validatePathInTempDir(path);
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在: " + path);
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("不是文件: " + path);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 写入文件内容
     *
     * @param path    文件路径（必须在临时目录下）
     * @param content 文件内容
     * @return 操作结果
     * @throws IOException       IO 异常
     * @throws SecurityException 如果路径不在临时目录下
     */
    @Tool(name = "writeFile", value = "Agent服务器上将文本内容写入指定文件（仅限临时目录），如果父目录不存在会自动创建")
    public String writeFile(
            @P(value = "要写入的文件路径（必须在临时目录下）", required = true) String path,
            @P(value = "要写入的文件内容", required = true) String content) throws IOException {
        // 安全校验：必须在临时目录下
        validatePathInTempDir(path);

        Path filePath = Paths.get(path);
        // 确保父目录存在
        Path parentDir = filePath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }
        Files.writeString(filePath, content, StandardCharsets.UTF_8);
        log.info("文件写入成功: {}", path);
        return "ok";
    }

    /**
     * 创建临时工作目录
     * 在系统临时目录下创建一个随机名称的目录
     *
     * @return 创建的目录完整路径
     * @throws IOException IO 异常
     */
    @Tool(name = "createTempDirectory", value = "Agent服务器上在系统临时目录下创建一个随机名称的工作目录，返回目录完整路径")
    public String createTempDirectory() throws IOException {
        // 在临时目录下创建随机名称的目录
        Path tempDir = Files.createTempDirectory("comfypilot_");
        String dirPath = tempDir.toString();

        // 将创建的目录添加到跟踪集合中，用于应用关闭时自动清理
        createdTempDirectories.add(dirPath);

        log.info("临时工作目录创建成功: {}", dirPath);
        log.debug("当前跟踪的临时目录数量: {}", createdTempDirectories.size());
        return dirPath;
    }

    /**
     * 列出目录内容
     *
     * @param path 目录路径
     * @return 文件和目录列表的 JSON 字符串
     * @throws IOException IO 异常
     */
    @Tool(name = "listDirectory", value = "Agent服务器上列出指定目录下的所有文件和子目录信息")
    public String listDirectory(@P(value = "要列出内容的目录路径", required = true) String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            throw new IOException("目录不存在: " + path);
        }
        if (!Files.isDirectory(dirPath)) {
            throw new IOException("不是目录: " + path);
        }

        List<FileInfo> fileInfos = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dirPath)) {
            stream.forEach(p -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                    fileInfos.add(new FileInfo(
                            p.getFileName().toString(),
                            p.toString(),
                            attrs.isDirectory(),
                            attrs.size(),
                            attrs.lastModifiedTime().toMillis()
                    ));
                } catch (IOException e) {
                    log.error("读取文件属性失败: {}", p, e);
                }
            });
        }

        // 将列表转换为 JSON 字符串返回
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < fileInfos.size(); i++) {
            FileInfo info = fileInfos.get(i);
            result.append(info.toJsonString());
            if (i < fileInfos.size() - 1) {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
     * 删除文件或目录
     *
     * @param path 文件或目录路径（必须在临时目录下）
     * @return 操作结果
     * @throws IOException       IO 异常
     * @throws SecurityException 如果路径不在临时目录下
     */
    @Tool(name = "delete", value = "Agent服务器上删除指定的文件或目录（仅限临时目录），如果是目录会递归删除所有内容")
    public String delete(@P(value = "要删除的文件或目录路径（必须在临时目录下）", required = true) String path) throws IOException {
        // 安全校验：必须在临时目录下
        validatePathInTempDir(path);

        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new IOException("路径不存在: " + path);
        }

        if (Files.isDirectory(filePath)) {
            // 递归删除目录
            Files.walkFileTree(filePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.delete(filePath);
        }

        createdTempDirectories.remove(path);
        log.info("删除成功: {}", path);
        return "ok";
    }

    /**
     * 移动或重命名文件/目录
     *
     * @param sourcePath 源路径（必须在临时目录下）
     * @param targetPath 目标路径（必须在临时目录下）
     * @return 操作结果
     * @throws IOException       IO 异常
     * @throws SecurityException 如果路径不在临时目录下
     */
    @Tool(name = "move", value = "Agent服务器上移动或重命名文件/目录（仅限临时目录），如果目标已存在会被覆盖")
    public String move(
            @P(value = "源文件或目录路径（必须在临时目录下）", required = true) String sourcePath,
            @P(value = "目标文件或目录路径（必须在临时目录下）", required = true) String targetPath) throws IOException {
        // 安全校验：源路径和目标路径都必须在临时目录下
        validatePathInTempDir(sourcePath);
        validatePathInTempDir(targetPath);

        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        if (!Files.exists(source)) {
            throw new IOException("源路径不存在: " + sourcePath);
        }

        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        createdTempDirectories.remove(sourcePath);
        log.info("移动成功: {} -> {}", sourcePath, targetPath);
        return "ok";
    }
}
