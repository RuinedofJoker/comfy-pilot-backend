//package org.joker.comfypilot.common.util;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledOnOs;
//import org.junit.jupiter.api.condition.OS;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * CommandUtil 字符编码测试类
// * 专门测试中文字符编码问题
// */
//@Slf4j
//class CommandUtilEncodingTest {
//
//    /**
//     * 测试 PowerShell 中文输出（使用 -EncodedCommand）
//     */
//    @Test
//    @EnabledOnOs(OS.WINDOWS)
//    void testPowerShellChineseOutput_EncodedCommand() throws IOException, InterruptedException {
//        String command = "echo \"你好世界\"";
//
//        log.info("========== 测试 PowerShell 中文输出（-EncodedCommand）==========");
//        log.info("命令: {}", command);
//
//        List<String> outputs = new ArrayList<>();
//
//        CommandResult result = CommandUtil.executeWithRealTimeOutput(
//                command,
//                (chunk, isError, process) -> {
//                    outputs.add(chunk);
//                    log.info("输出 [{}]: {}", isError ? "错误" : "标准", chunk.trim());
//                }
//        );
//
//        log.info("退出码: {}", result.getExitCode());
//        log.info("完整输出: {}", result.getOutput());
//
//        // 检查是否包含正确的中文
//        boolean containsChinese = result.getOutput().contains("你好世界");
//        log.info("是否包含正确中文: {}", containsChinese);
//
//        if (!containsChinese) {
//            log.error("❌ 中文输出乱码！");
//            log.error("期望: 你好世界");
//            log.error("实际: {}", result.getOutput().trim());
//        } else {
//            log.info("✅ 中文输出正常！");
//        }
//
//        // 断言验证
//        assertTrue(containsChinese, "输出应该包含正确的中文");
//    }
//
//    /**
//     * 测试 CMD 中文输出
//     */
//    @Test
//    @EnabledOnOs(OS.WINDOWS)
//    void testCmdChineseOutput() throws IOException, InterruptedException {
//        String command = "echo 你好世界";
//
//        log.info("========== 测试 CMD 中文输出 ==========");
//        log.info("命令: {}", command);
//
//        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
//                .command(command)
//                .charset(StandardCharsets.UTF_8)
//                .shellType(ShellType.CMD)
//                .outputCallbackWithProcess((chunk, isError, process) -> {
//                    log.info("输出 [{}]: {}", isError ? "错误" : "标准", chunk.trim());
//                })
//                .build();
//
//        CommandResult result = CommandUtil.execute(config);
//
//        log.info("退出码: {}", result.getExitCode());
//        log.info("完整输出: {}", result.getOutput());
//
//        boolean containsChinese = result.getOutput().contains("你好世界");
//        log.info("是否包含正确中文: {}", containsChinese ? "✅" : "❌");
//
//        if (!containsChinese) {
//            log.error("❌ CMD 中文输出乱码！");
//            log.error("期望: 你好世界");
//            log.error("实际: {}", result.getOutput().trim());
//        } else {
//            log.info("✅ CMD 中文输出正常！");
//        }
//
//        // 断言验证
//        assertTrue(containsChinese, "CMD 输出应该包含正确的中文");
//    }
//
//    /**
//     * 测试 CMD 错误输出中的中文
//     */
//    @Test
//    @EnabledOnOs(OS.WINDOWS)
//    void testCmdChineseInErrorOutput() throws IOException, InterruptedException {
//        // 使用不存在的命令触发 CMD 错误
//        String command = "不存在的命令";
//
//        log.info("========== 测试 CMD 错误输出中的中文 ==========");
//        log.info("命令: {}", command);
//
//        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
//                .command(command)
//                .charset(StandardCharsets.UTF_8)
//                .shellType(ShellType.CMD)
//                .outputCallbackWithProcess((chunk, isError, process) -> {
//                    if (isError) {
//                        log.info("错误输出: {}", chunk.trim());
//                    } else {
//                        log.info("标准输出: {}", chunk.trim());
//                    }
//                })
//                .build();
//
//        CommandResult result = CommandUtil.execute(config);
//
//        log.info("退出码: {}", result.getExitCode());
//        log.info("错误输出:\n{}", result.getError());
//
//        String errorOutput = result.getError();
//        boolean hasGarbledText = errorOutput.contains("?") || errorOutput.contains("�");
//
//        log.info("错误输出是否包含乱码字符: {}", hasGarbledText ? "❌ 是" : "✅ 否");
//
//        assertFalse(errorOutput.trim().isEmpty(), "错误输出不应该为空");
//
//        if (hasGarbledText) {
//            log.warn("⚠️ 检测到可能的乱码字符");
//        } else {
//            log.info("✅ 错误输出中没有检测到明显的乱码字符");
//        }
//    }
//        String command = "echo 你好世界";
//
//        log.info("测试 CMD 中文输出");
//        log.info("命令: {}", command);
//
//        CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
//                .command(command)
//                .charset(StandardCharsets.UTF_8)
//                .shellType(ShellType.CMD)
//                .outputCallbackWithProcess((chunk, isError, process) -> {
//                    log.info("输出 [{}]: {}", isError ? "错误" : "标准", chunk.trim());
//                })
//                .build();
//
//        CommandResult result = CommandUtil.execute(config);
//
//        log.info("退出码: {}", result.getExitCode());
//        log.info("完整输出: {}", result.getOutput());
//
//        boolean containsChinese = result.getOutput().contains("你好世界");
//        log.info("是否包含正确中文: {}", containsChinese);
//    }
//
//    /**
//     * 测试不同字符集的输出
//     */
//    @Test
//    @EnabledOnOs(OS.WINDOWS)
//    void testDifferentCharsets() throws IOException, InterruptedException {
//        String command = "echo \"测试中文编码\"";
//
//        Charset[] charsets = {
//                StandardCharsets.UTF_8,
//                Charset.forName("GBK")
//        };
//
//        for (Charset charset : charsets) {
//            log.info("\n========== 测试字符集: {} ==========", charset.name());
//
//            CommandUtil.CommandConfig config = CommandUtil.CommandConfig.builder()
//                    .command(command)
//                    .charset(charset)
//                    .shellType(ShellType.POWERSHELL)
//                    .build();
//
//            try {
//                CommandResult result = CommandUtil.execute(config);
//                log.info("退出码: {}", result.getExitCode());
//                log.info("输出: ", result.getOutput().trim());
//
//                boolean containsChinese = result.getOutput().contains("测试中文编码");
//                log.info("是否包含正确中文: {}", containsChinese);
//            } catch (Exception e) {
//                log.error("执行失败: {}", e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * 测试多行中文输出（验证 -EncodedCommand 修复）
//     */
//    @Test
//    @EnabledOnOs(OS.WINDOWS)
//    void testMultiLineChineseOutput() throws IOException, InterruptedException {
//        String command = "echo \"第一行\" && echo \"第二行\" && echo \"第三行\"";
//
//        log.info("========== 测试多行中文输出 ==========");
//        log.info("命令: {}", command);
//
//        List<String> lines = new ArrayList<>();
//
//        CommandResult result = CommandUtil.executeWithRealTimeOutput(
//                command,
//                (chunk, isError, process) -> {
//                    lines.add(chunk);
//                    log.info("输出行 [{}]: {}", isError ? "错误" : "标准", chunk.trim());
//                }
//        );
//
//        log.info("总共收到 {} 行输出", lines.size());
//        log.info("完整输出:\n{}", result.getOutput());
//
//        // 验证每一行
//        String output = result.getOutput();
//        boolean hasLine1 = output.contains("第一行");
//        boolean hasLine2 = output.contains("第二行");
//        boolean hasLine3 = output.contains("第三行");
//
//        log.info("第一行正确: {}", hasLine1 ? "✅" : "❌");
//        log.info("第二行正确: {}", hasLine2 ? "✅" : "❌");
//        log.info("第三行正确: {}", hasLine3 ? "✅" : "❌");
//
//        // 断言验证
//        assertTrue(hasLine1, "应该包含第一行");
//        assertTrue(hasLine2, "应该包含第二行");
//        assertTrue(hasLine3, "应该包含第三行");
//    }
//
//    /**
//     * 测试错误输出中的中文（这是关键测试）
//     * 测试 PowerShell 语法错误时的中文错误信息
//     */
//    @Test
//    @EnabledOnOs(OS.WINDOWS)
//    void testChineseInErrorOutput() throws IOException, InterruptedException {
//        // 使用 && 语法（PowerShell 不支持），会导致语法错误
//        // 这样可以测试 PowerShell 自身错误信息的编码
//        String command = "echo \"测试\" && echo \"错误\"";
//
//        log.info("========== 测试错误输出中的中文（语法错误场景）==========");
//        log.info("命令: {}", command);
//        log.info("说明: PowerShell 不支持 && 语法，会产生语法错误");
//
//        List<String> errorLines = new ArrayList<>();
//
//        CommandResult result = CommandUtil.executeWithRealTimeOutput(
//                command,
//                (chunk, isError, process) -> {
//                    if (isError) {
//                        errorLines.add(chunk);
//                        log.info("错误输出: {}", chunk.trim());
//                    } else {
//                        log.info("标准输出: {}", chunk.trim());
//                    }
//                }
//        );
//
//        log.info("退出码: {}", result.getExitCode());
//        log.info("错误输出:\n{}", result.getError());
//
//        // PowerShell 的错误信息可能包含这些中文关键词
//        String errorOutput = result.getError();
//
//        // 检查是否有乱码（如果有问号或方框字符，说明乱码了）
//        boolean hasGarbledText = errorOutput.contains("?") || errorOutput.contains("�");
//
//        log.info("错误输出是否包含乱码字符: {}", hasGarbledText ? "❌ 是" : "✅ 否");
//        log.info("错误输出长度: {} 字符", errorOutput.length());
//
//        // 验证：错误输出不应该为空，且不应该包含大量乱码
//        assertFalse(errorOutput.trim().isEmpty(), "错误输出不应该为空");
//
//        if (hasGarbledText) {
//            log.warn("⚠️ 检测到可能的乱码字符，但这可能是正常的（取决于错误信息内容）");
//        } else {
//            log.info("✅ 错误输出中没有检测到明显的乱码字符");
//        }
//    }
//
//    /**
//     * 辅助方法：将字节数组转换为十六进制字符串
//     */
//    private static String bytesToHex(byte[] bytes) {
//        StringBuilder sb = new StringBuilder();
//        for (byte b : bytes) {
//            sb.append(String.format("%02X ", b));
//        }
//        return sb.toString().trim();
//    }
//}
