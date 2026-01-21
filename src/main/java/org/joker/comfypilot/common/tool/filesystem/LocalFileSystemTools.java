package org.joker.comfypilot.common.tool.filesystem;

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
import java.util.stream.Stream;

/**
 * 本地文件系统操作服务
 * 提供安全的本地文件系统访问功能，类似于 MCP filesystem server
 */
@Slf4j
@Component
@ToolSet("local_filesystem_")
public class LocalFileSystemTools {

    /**
     * 检查路径是否存在
     *
     * @param path 文件或目录路径
     * @return 是否存在
     */
    @Tool(name = "exists", value = "检查指定路径的文件或目录是否存在")
    public boolean exists(@P(value = "文件或目录的完整路径", required = true) String path) {
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
    @Tool(name = "isDirectory", value = "检查指定路径是否为目录")
    public boolean isDirectory(@P(value = "要检查的路径", required = true) String path) {
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
    @Tool(name = "isFile", value = "检查指定路径是否为普通文件")
    public boolean isFile(@P(value = "要检查的路径", required = true) String path) {
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
    @Tool(name = "readFile", value = "读取指定文件的文本内容")
    public String readFile(@P(value = "要读取的文件路径", required = true) String path) throws IOException {
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
     * @param path    文件路径
     * @param content 文件内容
     * @return 操作结果
     * @throws IOException IO 异常
     */
    @Tool(name = "writeFile", value = "将文本内容写入指定文件，如果父目录不存在会自动创建")
    public String writeFile(
            @P(value = "要写入的文件路径", required = true) String path,
            @P(value = "要写入的文件内容", required = true) String content) throws IOException {
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
     * 创建目录
     *
     * @param path 目录路径
     * @return 操作结果
     * @throws IOException IO 异常
     */
    @Tool(name = "createDirectory", value = "创建目录，如果父目录不存在会递归创建")
    public String createDirectory(@P(value = "要创建的目录路径", required = true) String path) throws IOException {
        Path dirPath = Paths.get(path);
        if (Files.exists(dirPath)) {
            if (!Files.isDirectory(dirPath)) {
                throw new IOException("路径已存在且不是目录: " + path);
            }
            log.info("目录已存在: {}", path);
            return "ok";
        }
        Files.createDirectories(dirPath);
        log.info("目录创建成功: {}", path);
        return "ok";
    }

    /**
     * 列出目录内容
     *
     * @param path 目录路径
     * @return 文件和目录列表的 JSON 字符串
     * @throws IOException IO 异常
     */
    @Tool(name = "listDirectory", value = "列出指定目录下的所有文件和子目录信息")
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
     * @param path 文件或目录路径
     * @return 操作结果
     * @throws IOException IO 异常
     */
    @Tool(name = "delete", value = "删除指定的文件或目录，如果是目录会递归删除所有内容")
    public String delete(@P(value = "要删除的文件或目录路径", required = true) String path) throws IOException {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new IOException("路径不存在: " + path);
        }

        if (Files.isDirectory(filePath)) {
            // 递归删除目录
            Files.walkFileTree(filePath, new SimpleFileVisitor<Path>() {
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
        log.info("删除成功: {}", path);
        return "ok";
    }

    /**
     * 移动或重命名文件/目录
     *
     * @param sourcePath 源路径
     * @param targetPath 目标路径
     * @return 操作结果
     * @throws IOException IO 异常
     */
    @Tool(name = "move", value = "移动或重命名文件/目录，如果目标已存在会被覆盖")
    public String move(
            @P(value = "源文件或目录路径", required = true) String sourcePath,
            @P(value = "目标文件或目录路径", required = true) String targetPath) throws IOException {
        Path source = Paths.get(sourcePath);
        Path target = Paths.get(targetPath);

        if (!Files.exists(source)) {
            throw new IOException("源路径不存在: " + sourcePath);
        }

        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("移动成功: {} -> {}", sourcePath, targetPath);
        return "ok";
    }
}
