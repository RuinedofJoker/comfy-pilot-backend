package org.joker.comfypilot.cfsvr.infrastructure.filesystem;

import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.common.tool.filesystem.FileInfo;
import org.joker.comfypilot.common.tool.filesystem.LocalFileSystemTools;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

/**
 * 本地文件系统服务测试类
 * 测试本地文件系统的基本操作功能
 */
public class LocalFileSystemToolsTest extends BaseTest {

    private final LocalFileSystemTools fileSystemService = new LocalFileSystemTools();

    /**
     * 测试文件读写操作
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 创建临时文件</li>
     *   <li>2. 写入文件内容</li>
     *   <li>3. 读取文件内容</li>
     *   <li>4. 验证内容一致性</li>
     *   <li>5. 清理临时文件</li>
     * </ul>
     */
    @Test
    public void testFileReadWrite() throws IOException {
        System.out.println("=== 测试文件读写操作 ===");

        // 1. 创建临时文件路径
        String tempDir = System.getProperty("java.io.tmpdir");
        String testFilePath = Paths.get(tempDir, "comfy-pilot-test.txt").toString();
        System.out.println("临时文件路径: " + testFilePath);

        try {
            // 2. 写入文件内容
            String content = "Hello, ComfyPilot!\nThis is a test file.";
            System.out.println("\n>>> 写入文件内容:");
            System.out.println(content);
            fileSystemService.writeFile(testFilePath, content);

            // 3. 检查文件是否存在
            boolean exists = fileSystemService.exists(testFilePath);
            System.out.println("\n文件是否存在: " + exists);

            // 4. 读取文件内容
            String readContent = fileSystemService.readFile(testFilePath);
            System.out.println("\n>>> 读取文件内容:");
            System.out.println(readContent);

            // 5. 验证内容一致性
            if (content.equals(readContent)) {
                System.out.println("\n✅ 文件读写测试通过！");
            } else {
                System.err.println("\n❌ 文件内容不一致！");
            }

        } finally {
            // 6. 清理临时文件
            if (fileSystemService.exists(testFilePath)) {
                fileSystemService.delete(testFilePath);
                System.out.println("\n临时文件已清理");
            }
        }
    }

    /**
     * 测试目录操作
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 创建目录</li>
     *   <li>2. 在目录中创建文件</li>
     *   <li>3. 列出目录内容</li>
     *   <li>4. 清理测试目录</li>
     * </ul>
     */
    @Test
    public void testDirectoryOperations() throws IOException {
        System.out.println("\n=== 测试目录操作 ===");

        // 1. 创建临时目录路径
        String tempDir = System.getProperty("java.io.tmpdir");
        String testDirPath = Paths.get(tempDir, "comfy-pilot-test-dir").toString();
        System.out.println("临时目录路径: " + testDirPath);

        try {
            // 2. 创建目录
            fileSystemService.createDirectory(testDirPath);
            System.out.println("\n✅ 目录创建成功");

            // 3. 在目录中创建几个测试文件
            fileSystemService.writeFile(Paths.get(testDirPath, "file1.txt").toString(), "Content 1");
            fileSystemService.writeFile(Paths.get(testDirPath, "file2.txt").toString(), "Content 2");
            fileSystemService.createDirectory(Paths.get(testDirPath, "subdir").toString());
            System.out.println("✅ 测试文件创建成功");

            // 4. 列出目录内容
            List<FileInfo> files = fileSystemService.listDirectory(testDirPath);
            System.out.println("\n>>> 目录内容 (" + files.size() + " 项):");
            for (FileInfo file : files) {
                String type = file.getIsDirectory() ? "[目录]" : "[文件]";
                System.out.println(type + " " + file.getName() + " (" + file.getSize() + " 字节)");
            }

        } finally {
            // 5. 清理测试目录
            if (fileSystemService.exists(testDirPath)) {
                fileSystemService.delete(testDirPath);
                System.out.println("\n临时目录已清理");
            }
        }
    }
}