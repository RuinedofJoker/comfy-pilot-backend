package org.joker.comfypilot.common.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandUtil 测试类
 * 模仿 ComfyUILocalCommandTools.executeCommand 方法中调用 CommandUtil 的方式
 */
@Slf4j
class CommandUtilTest {

    /**
     * 测试基本命令执行（无实时输出）
     */
    @Test
    void testExecuteBasicCommand() throws IOException, InterruptedException {
        // 根据操作系统选择不同的命令
        String command = System.getProperty("os.name").toLowerCase().contains("windows")
                ? "echo Hello World"
                : "echo 'Hello World'";

        log.info("开始执行基本命令: {}", command);

        // 执行命令
        CommandResult result = CommandUtil.execute(command);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getExitCode(), "命令应该成功执行");
        assertTrue(result.isSuccess(), "命令执行状态应该为成功");
        assertTrue(result.getOutput().contains("Hello World"), "输出应该包含 'Hello World'");

        log.info("命令执行完成，退出码: {}, 输出: {}", result.getExitCode(), result.getOutput().trim());
    }

    /**
     * 测试带工作目录的命令执行
     */
    @Test
    void testExecuteCommandWithWorkingDir() throws IOException, InterruptedException {
        // 使用系统临时目录作为工作目录
        String workingDir = System.getProperty("java.io.tmpdir");
        String command = System.getProperty("os.name").toLowerCase().contains("windows")
                ? "cd"
                : "pwd";

        log.info("开始执行命令（指定工作目录）: {}, 工作目录: {}", command, workingDir);

        // 执行命令
        CommandResult result = CommandUtil.execute(command, workingDir);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getExitCode(), "命令应该成功执行");
        assertTrue(result.isSuccess(), "命令执行状态应该为成功");
        assertFalse(result.getOutput().trim().isEmpty(), "输出不应该为空");

        log.info("命令执行完成，退出码: {}, 输出: {}", result.getExitCode(), result.getOutput().trim());
    }

    /**
     * 测试实时输出回调（模仿 ComfyUILocalCommandTools.executeCommand 的方式）
     * 这是核心测试，完全模仿 ComfyUILocalCommandTools 中的调用方式
     */
    @Test
    void testExecuteWithRealTimeOutput_MimicComfyUILocalCommandTools() throws IOException, InterruptedException {
        // 准备测试数据
        String command = System.getProperty("os.name").toLowerCase().contains("windows")
                ? "echo \"你好1\" ; echo \"你好2\" ; echo \"你好3\""
                : "echo '\"你好1\"' && echo '\"你好2\"' && echo '\"你好3\"'";

        String workingDir = System.getProperty("java.io.tmpdir");

        // 用于收集输出的列表
        List<String> outputLines = new ArrayList<>();
        List<String> errorLines = new ArrayList<>();

        // 模拟中断标志（模仿 ComfyUILocalCommandTools 中的 interrupted）
        AtomicBoolean interrupted = new AtomicBoolean(false);

        log.info("开始执行命令（实时输出）: {}", command);

        // 执行命令 - 完全模仿 ComfyUILocalCommandTools.executeCommand 的调用方式
        CommandResult result;
        if (workingDir != null && !workingDir.trim().isEmpty()) {
            result = CommandUtil.executeWithRealTimeOutput(
                    command,
                    workingDir.trim(),
                    (chunk, isError, process) -> {
                        // 模仿 ComfyUILocalCommandTools 中的回调逻辑
                        String prefix = isError ? "1 " : "0 ";
                        String output = prefix + chunk;

                        // 收集输出用于验证
                        if (isError) {
                            errorLines.add(chunk);
                        } else {
                            outputLines.add(chunk);
                        }

                        System.out.printf(chunk);

                        // 模拟中断检测（模仿 ComfyUILocalCommandTools 中的中断逻辑）
                        if (interrupted.get()) {
                            process.destroy();
                        }
                    }
            );
        } else {
            result = CommandUtil.executeWithRealTimeOutput(
                    command,
                    (chunk, isError, process) -> {
                        String prefix = isError ? "1 " : "0 ";
                        String output = prefix + chunk;

                        if (isError) {
                            errorLines.add(chunk);
                        } else {
                            outputLines.add(chunk);
                        }

                        log.debug("实时输出: {}", output.trim());

                        if (interrupted.get()) {
                            process.destroy();
                        }
                    }
            );
        }

        // 验证结果 - 模仿 ComfyUILocalCommandTools 中的结果处理
        assertNotNull(result);
        assertEquals(0, result.getExitCode(), "命令应该成功执行");
        assertTrue(result.isSuccess(), "命令执行状态应该为成功");

        // 验证实时输出被正确收集
        assertFalse(outputLines.isEmpty(), "应该收集到输出");

        // 构建返回结果（模仿 ComfyUILocalCommandTools 的格式）
        StringBuilder output = new StringBuilder();
        output.append("命令执行完成\n");
        output.append("退出码: ").append(result.getExitCode()).append("\n");
        output.append("执行状态: ").append(result.isSuccess() ? "成功" : "失败").append("\n");
        output.append("\n");

        // 添加标准输出
        if (result.getOutput() != null && !result.getOutput().trim().isEmpty()) {
            output.append("标准输出:\n");
            output.append(result.getOutput().trim()).append("\n");
        }

        // 添加错误输出
        if (result.getError() != null && !result.getError().trim().isEmpty()) {
            output.append("\n错误输出:\n");
            output.append(result.getError().trim()).append("\n");
        }

        String resultStr = output.toString();
        log.info("命令执行完成，退出码: {}, 输出长度: {} 字符",
                result.getExitCode(), resultStr.length());
        log.info("格式化输出:\n{}", resultStr);

        // 验证输出格式
        assertTrue(resultStr.contains("命令执行完成"));
        assertTrue(resultStr.contains("退出码: 0"));
        assertTrue(resultStr.contains("执行状态: 成功"));
    }

    /**
     * 测试实时输出回调（简化版本，不指定工作目录）
     */
    @Test
    void testExecuteWithRealTimeOutput_NoWorkingDir() throws IOException, InterruptedException {
        String command = System.getProperty("os.name").toLowerCase().contains("windows")
                ? "echo Test Output"
                : "echo 'Test Output'";

        List<String> outputLines = new ArrayList<>();
        AtomicBoolean interrupted = new AtomicBoolean(false);

        log.info("开始执行命令（实时输出，无工作目录）: {}", command);

        // 模仿 ComfyUILocalCommandTools 中不指定工作目录的情况
        CommandResult result = CommandUtil.executeWithRealTimeOutput(
                command,
                (chunk, isError, process) -> {
                    outputLines.add((isError ? "1 " : "0 ") + chunk);
                    log.debug("实时输出: {}", chunk.trim());

                    if (interrupted.get()) {
                        process.destroy();
                    }
                }
        );

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.isSuccess());
        assertFalse(outputLines.isEmpty());

        log.info("命令执行完成，收集到 {} 行输出", outputLines.size());
    }

    /**
     * 测试命令执行失败的情况
     */
    @Test
    void testExecuteFailedCommand() throws IOException, InterruptedException {
        // 使用一个不存在的命令
        String command = "nonexistentcommand12345";

        log.info("开始执行失败命令: {}", command);

        try {
            CommandResult result = CommandUtil.execute(command);

            // 命令应该失败
            assertNotNull(result);
            assertNotEquals(0, result.getExitCode(), "命令应该失败");
            assertFalse(result.isSuccess(), "命令执行状态应该为失败");

            log.info("命令执行失败（预期），退出码: {}", result.getExitCode());
        } catch (IOException e) {
            // 某些系统可能会抛出异常而不是返回非零退出码
            log.info("命令执行抛出异常（预期）: {}", e.getMessage());
        }
    }

    /**
     * 测试多行输出命令（Windows 专用）
     */
    @Test
    @EnabledOnOs(OS.WINDOWS)
    void testExecuteMultiLineCommand_Windows() throws IOException, InterruptedException {
        String command = "echo Line1 && echo Line2 && echo Line3";

        log.info("开始执行多行命令（Windows）: {}", command);

        CommandResult result = CommandUtil.execute(command);

        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.getOutput().contains("Line1"));
        assertTrue(result.getOutput().contains("Line2"));
        assertTrue(result.getOutput().contains("Line3"));

        log.info("命令执行完成，输出:\n{}", result.getOutput());
    }

    /**
     * 测试多行输出命令（Linux/Mac 专用）
     */
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void testExecuteMultiLineCommand_Unix() throws IOException, InterruptedException {
        String command = "echo 'Line1' && echo 'Line2' && echo 'Line3'";

        log.info("开始执行多行命令（Unix）: {}", command);

        CommandResult result = CommandUtil.execute(command);

        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.getOutput().contains("Line1"));
        assertTrue(result.getOutput().contains("Line2"));
        assertTrue(result.getOutput().contains("Line3"));

        log.info("命令执行完成，输出:\n{}", result.getOutput());
    }

    /**
     * 测试环境初始化脚本 + 用户命令的组合（模仿 buildFinalCommand 的逻辑）
     */
    @Test
    void testExecuteWithEnvironmentInitScript() throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

        // 模拟环境初始化脚本
        String environmentInitScript = isWindows
                ? "set TEST_VAR=HelloWorld"
                : "export TEST_VAR=HelloWorld";

        // 用户命令
        String userCommand = isWindows
                ? "echo %TEST_VAR%"
                : "echo $TEST_VAR";

        // 构建最终命令（模仿 ComfyUILocalCommandTools.buildFinalCommand）
        String finalCommand = environmentInitScript + " && " + userCommand;

        log.info("开始执行组合命令: {}", finalCommand);

        CommandResult result = CommandUtil.execute(finalCommand);

        assertNotNull(result);
        assertEquals(0, result.getExitCode());
        assertTrue(result.getOutput().contains("HelloWorld"), "输出应该包含环境变量的值");

        log.info("命令执行完成，输出: {}", result.getOutput().trim());
    }
}
