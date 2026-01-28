package org.joker.comfypilot.ssh;

import org.joker.comfypilot.BaseTest;
import org.joker.comfypilot.common.util.CommandResult;
import org.joker.comfypilot.common.util.CommandUtil;
import org.joker.comfypilot.common.util.ShellType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandUtil 工具类测试
 * 测试本地命令执行的各种功能
 */
public class CommandUtilTest extends BaseTest {

    /**
     * 测试简单命令执行（跨平台）
     */
    @Test
    public void testSimpleCommand() throws IOException, InterruptedException {
        System.out.println("=== 测试简单命令执行 ===");

        // 根据操作系统选择不同的命令
        String command = isWindows() ? "Write-Output 'Hello'" : "echo 'Hello'";

        CommandResult result = CommandUtil.execute(command);

        System.out.println("退出码: " + result.getExitCode());
        System.out.println("输出: " + result.getOutput());

        // 验证结果
        assertTrue(result.isSuccess(), "命令应该执行成功");
        assertTrue(result.getOutput().contains("Hello"), "输出应该包含 'Hello'");
        System.out.println("✅ 简单命令执行测试通过\n");
    }

    /**
     * 测试工作目录设置
     */
    @Test
    public void testWorkingDirectory() throws IOException, InterruptedException {
        System.out.println("=== 测试工作目录设置 ===");

        // 获取用户主目录
        String homeDir = System.getProperty("user.home");
        System.out.println("用户主目录: " + homeDir);

        // 在主目录下执行 pwd 命令（PowerShell 和 Bash 都支持）
        String command = "pwd";
        CommandResult result = CommandUtil.execute(command, homeDir);

        System.out.println("退出码: " + result.getExitCode());
        System.out.println("当前目录: " + result.getOutput().trim());

        // 验证结果
        assertTrue(result.isSuccess(), "命令应该执行成功");
        assertFalse(result.getOutput().trim().isEmpty(), "输出不应该为空");
        System.out.println("✅ 工作目录设置测试通过\n");
    }

    /**
     * 测试超时控制
     */
    @Test
    public void testTimeout() {
        System.out.println("=== 测试超时控制 ===");

        // 创建一个会超时的命令（睡眠 10 秒，但超时设置为 2 秒）
        // Windows PowerShell 使用 Start-Sleep，Linux/Mac 使用 sleep
        String command = isWindows() ? "Start-Sleep -Seconds 10" : "sleep 10";

        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
                .command(command)
                .timeout(2)  // 2 秒超时
                .build();

        // 验证会抛出超时异常
        IOException exception = assertThrows(IOException.class, () -> {
            CommandUtil.execute(config);
        });

        System.out.println("捕获到预期的超时异常: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("超时"), "异常消息应该包含'超时'");
        System.out.println("✅ 超时控制测试通过\n");
    }

    /**
     * 测试实时输出回调
     */
    @Test
    public void testRealTimeOutput() throws IOException, InterruptedException {
        System.out.println("=== 测试实时输出回调 ===");

        AtomicInteger lineCount = new AtomicInteger(0);

        // 执行一个会产生多行输出的命令
        String command = isWindows()
                ? "Write-Output 'Line 1'; Write-Output 'Line 2'; Write-Output 'Line 3'"
                : "echo 'Line 1'; echo 'Line 2'; echo 'Line 3'";

        CommandResult result = CommandUtil.executeWithRealTimeOutput(command, (line, isError) -> {
            System.out.println("实时输出 [" + lineCount.incrementAndGet() + "]: " + line);
        });

        System.out.println("\n完整输出:\n" + result.getOutput());
        System.out.println("总行数: " + lineCount.get());

        // 验证结果
        assertTrue(result.isSuccess(), "命令应该执行成功");
        assertTrue(lineCount.get() >= 3, "至少应该有 3 行输出");
        System.out.println("✅ 实时输出回调测试通过\n");
    }

    /**
     * 测试环境变量设置
     */
    @Test
    public void testEnvironmentVariables() throws IOException, InterruptedException {
        System.out.println("=== 测试环境变量设置 ===");

        // 设置自定义环境变量并读取
        // Windows PowerShell 使用 $env:VAR，Linux/Mac 使用 $VAR
        String command = isWindows()
                ? "Write-Output $env:TEST_VAR"
                : "echo $TEST_VAR";

        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
                .command(command)
                .addEnvironment("TEST_VAR", "Hello from environment")
                .build();

        CommandResult result = CommandUtil.execute(config);

        System.out.println("退出码: " + result.getExitCode());
        System.out.println("输出: " + result.getOutput());

        // 验证结果
        assertTrue(result.isSuccess(), "命令应该执行成功");
        assertTrue(result.getOutput().contains("Hello from environment"),
                "输出应该包含环境变量的值");
        System.out.println("✅ 环境变量设置测试通过\n");
    }

    /**
     * 测试错误输出捕获
     */
    @Test
    public void testErrorOutput() throws IOException, InterruptedException {
        System.out.println("=== 测试错误输出捕获 ===");

        // 执行一个会产生错误的命令
        String command = isWindows()
                ? "[Console]::Error.WriteLine('Error message')"
                : "echo 'Error message' >&2";

        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
                .command(command)
                .redirectErrorStream(false)  // 不合并错误流
                .build();

        CommandResult result = CommandUtil.execute(config);

        System.out.println("退出码: " + result.getExitCode());
        System.out.println("标准输出: " + result.getOutput());
        System.out.println("错误输出: " + result.getError());

        // 验证结果
        assertTrue(result.isSuccess(), "命令应该执行成功");
        assertTrue(result.getError().contains("Error message"),
                "错误输出应该包含错误消息");
        System.out.println("✅ 错误输出捕获测试通过\n");
    }

    /**
     * 测试字符编码
     */
    @Test
    public void testCharsetEncoding() throws IOException, InterruptedException {
        System.out.println("=== 测试字符编码 ===");

        // 输出中文字符
        String command = isWindows()
                ? "Write-Output '你好，世界'"
                : "echo '你好，世界'";

        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
                .command(command)
                .charset(StandardCharsets.UTF_8)
                .build();

        CommandResult result = CommandUtil.execute(config);

        System.out.println("退出码: " + result.getExitCode());
        System.out.println("输出: " + result.getOutput());

        // 验证结果
        assertTrue(result.isSuccess(), "命令应该执行成功");
        System.out.println("✅ 字符编码测试通过\n");
    }

    /**
     * 测试指定 Shell 类型
     */
    @Test
    public void testShellType() throws IOException, InterruptedException {
        System.out.println("=== 测试指定 Shell 类型 ===");

        if (isWindows()) {
            // Windows: 测试 PowerShell
            CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
                    .command("Get-Date")
                    .shellType(ShellType.POWERSHELL)
                    .build();

            CommandResult result = CommandUtil.execute(config);

            System.out.println("PowerShell 输出: " + result.getOutput());
            assertTrue(result.isSuccess(), "PowerShell 命令应该执行成功");
        } else {
            // Linux/Mac: 测试 Bash
            CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
                    .command("date")
                    .shellType(ShellType.BASH)
                    .build();

            CommandResult result = CommandUtil.execute(config);

            System.out.println("Bash 输出: " + result.getOutput());
            assertTrue(result.isSuccess(), "Bash 命令应该执行成功");
        }

        System.out.println("✅ Shell 类型测试通过\n");
    }

    /**
     * 测试命令失败场景
     */
    @Test
    public void testCommandFailure() throws IOException, InterruptedException {
        System.out.println("=== 测试命令失败场景 ===");

        // 执行一个不存在的命令
        String command = "this_command_does_not_exist_12345";

        CommandResult result = CommandUtil.execute(command);

        System.out.println("退出码: " + result.getExitCode());
        System.out.println("输出: " + result.getOutput());
        System.out.println("错误: " + result.getError());

        // 验证结果
        assertFalse(result.isSuccess(), "命令应该执行失败");
        assertNotEquals(0, result.getExitCode(), "退出码应该非零");
        System.out.println("✅ 命令失败场景测试通过\n");
    }

    /**
     * 辅助方法：判断是否为 Windows 系统
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
