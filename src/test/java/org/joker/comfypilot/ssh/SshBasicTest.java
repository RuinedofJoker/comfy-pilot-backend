package org.joker.comfypilot.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joker.comfypilot.BaseTest;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * SSH 基本功能测试类
 * 测试 Apache MINA SSHD 客户端的基本连接和命令执行功能
 */
public class SshBasicTest extends BaseTest {

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 测试 SSH 密码认证连接
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 使用用户名密码连接到 SSH 服务器</li>
     *   <li>2. 执行简单的 shell 命令</li>
     *   <li>3. 获取命令执行结果</li>
     *   <li>4. 正确关闭连接</li>
     * </ul>
     *
     * <p>环境变量配置：</p>
     * <pre>
     * SSH_HOST=your-ssh-host
     * SSH_PORT=22
     * SSH_USERNAME=your-username
     * SSH_PASSWORD=your-password
     * </pre>
     */
    @Test
    public void testSshPasswordAuthentication() throws IOException {
        // 1. 从环境变量加载 SSH 配置
        String sshHost = System.getProperty("SSH_HOST");
        int sshPort = Integer.parseInt(System.getProperty("SSH_PORT", "22"));
        String sshUsername = System.getProperty("SSH_USERNAME");
        String sshPassword = System.getProperty("SSH_PASSWORD");

        // 创建 SSH 客户端
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        try {
            // 连接到 SSH 服务器
            System.out.println("正在连接到 SSH 服务器...");
            ClientSession session = client.connect(sshUsername, sshHost, sshPort)
                    .verify(10, TimeUnit.SECONDS)
                    .getSession();

            // 使用密码认证
            session.addPasswordIdentity(sshPassword);
            session.auth().verify(10, TimeUnit.SECONDS);
            System.out.println("✅ SSH 连接成功！");
            System.out.println();

            // 执行测试命令
            executeCommand(session, "pwd");
            executeCommand(session, "whoami");
            executeCommand(session, "uname -a");

            // 关闭会话
            session.close();
            System.out.println("✅ SSH 会话已关闭");

        } catch (Exception e) {
            System.err.println("❌ SSH 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止客户端
            client.stop();
        }
    }

    /**
     * 测试 SSH Shell 通道（批量命令执行）
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 创建交互式 Shell 通道</li>
     *   <li>2. 在同一个 Shell 会话中执行多条命令</li>
     *   <li>3. 支持环境变量和工作目录切换</li>
     *   <li>4. 获取所有命令的输出</li>
     * </ul>
     *
     * <p>注意：此方法使用 ByteArrayInputStream 预先准备所有命令，不是真正的实时交互</p>
     *
     * <p>Shell 通道 vs Exec 通道：</p>
     * <ul>
     *   <li>Shell 通道：交互式会话，命令之间共享环境（环境变量、工作目录等）</li>
     *   <li>Exec 通道：每次执行独立命令，命令之间不共享环境</li>
     * </ul>
     *
     * <p>环境变量配置：</p>
     * <pre>
     * SSH_HOST=your-ssh-host
     * SSH_PORT=22
     * SSH_USERNAME=your-username
     * SSH_PASSWORD=your-password
     * </pre>
     */
    @Test
    public void testSshShellChannel() throws IOException {
        // 1. 从环境变量加载 SSH 配置
        String sshHost = System.getProperty("SSH_HOST");
        int sshPort = Integer.parseInt(System.getProperty("SSH_PORT", "22"));
        String sshUsername = System.getProperty("SSH_USERNAME");
        String sshPassword = System.getProperty("SSH_PASSWORD");

        // 创建 SSH 客户端
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        try {
            // 连接到 SSH 服务器
            System.out.println("正在连接到 SSH 服务器...");
            ClientSession session = client.connect(sshUsername, sshHost, sshPort)
                    .verify(10, TimeUnit.SECONDS)
                    .getSession();

            // 使用密码认证
            session.addPasswordIdentity(sshPassword);
            session.auth().verify(10, TimeUnit.SECONDS);
            System.out.println("✅ SSH 连接成功！");
            System.out.println();

            System.out.println(session.isOpen());

            // 执行 Shell 通道测试
            executeShellCommands(session);

            // 关闭会话
            session.close();
            System.out.println("✅ SSH 会话已关闭");

            System.out.println(session.isOpen());

        } catch (Exception e) {
            System.err.println("❌ SSH 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止客户端
            client.stop();
        }
    }

    /**
     * 执行 SSH 命令并打印结果（Exec 通道）
     *
     * @param session SSH 会话
     * @param command 要执行的命令
     * @throws IOException IO 异常
     */
    private void executeCommand(ClientSession session, String command) throws IOException {
        System.out.println(">>> 执行命令: " + command);

        try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
             ClientChannel channel = session.createExecChannel(command)) {

            // 设置输出流
            channel.setOut(responseStream);
            channel.setErr(errorStream);

            // 打开通道并等待命令执行完成
            channel.open().verify(10, TimeUnit.SECONDS);
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(10));

            // 获取命令执行结果
            String response = responseStream.toString(StandardCharsets.UTF_8);
            String error = errorStream.toString(StandardCharsets.UTF_8);

            // 打印结果
            if (!response.isEmpty()) {
                System.out.println(response.trim());
            }
            if (!error.isEmpty()) {
                System.err.println("错误输出: " + error.trim());
            }

            // 获取退出状态
            Integer exitStatus = channel.getExitStatus();
            System.out.println("退出状态: " + exitStatus);
            System.out.println();

        } catch (Exception e) {
            System.err.println("❌ 命令执行失败: " + e.getMessage());
            throw new IOException("命令执行失败", e);
        }
    }

    /**
     * 使用 Shell 通道执行多条命令
     * Shell 通道特点：命令之间共享环境（环境变量、工作目录等）
     *
     * @param session SSH 会话
     * @throws IOException IO 异常
     */
    private void executeShellCommands(ClientSession session) throws IOException {
        System.out.println("=== 开始 Shell 通道测试 ===");
        System.out.println();

        // 准备要执行的命令序列
        String[] commands = {
                "pwd",                          // 打印当前目录
                "whoami",                       // 打印当前用户
                "export TEST_VAR=HelloWorld",   // 设置环境变量
                "echo $TEST_VAR",               // 读取环境变量（验证环境共享）
                "cd /tmp",                      // 切换目录
                "pwd",                          // 再次打印目录（验证目录切换生效）
                "ls -la | head -5",             // 列出文件
//                "exit"                          // 退出 Shell
        };

        // 将所有命令拼接成一个字符串（每条命令后加换行符）
        StringBuilder commandScript = new StringBuilder();
        System.out.println(">>> 执行命令序列:");
        for (String command : commands) {
            System.out.println("    " + command);
            commandScript.append(command).append("\n");
        }
        System.out.println();

        // 将命令字符串转换为输入流
        ByteArrayInputStream inputStream = new ByteArrayInputStream(
                commandScript.toString().getBytes(StandardCharsets.UTF_8)
        );

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
             ClientChannel channel = session.createShellChannel()) {

            // 设置输入输出流
            channel.setIn(inputStream);        // 设置输入流（命令）
            channel.setOut(outputStream);      // 设置标准输出
            channel.setErr(errorStream);       // 设置错误输出

            // 打开 Shell 通道
            channel.open().verify(10, TimeUnit.SECONDS);
            System.out.println("✅ Shell 通道已打开");
            System.out.println();

            // 等待命令执行完成（不等待通道关闭）
            System.out.println("等待命令执行完成...");

            // 方案1：等待输入流被完全读取（EOF事件）
            Set<ClientChannelEvent> events = channel.waitFor(
                    EnumSet.of(ClientChannelEvent.EOF),
                    TimeUnit.SECONDS.toMillis(5)
            );

            if (events.contains(ClientChannelEvent.EOF)) {
                System.out.println("✅ 输入流已读取完毕");
            }

            // 方案2：额外等待一段时间，确保命令执行完成并输出结果
            Thread.sleep(2000);  // 等待2秒让命令执行完成

            // 获取所有输出
            String output = outputStream.toString(StandardCharsets.UTF_8);
            String error = errorStream.toString(StandardCharsets.UTF_8);

            // 打印输出
            System.out.println("=== Shell 输出 ===");
            if (!output.isEmpty()) {
                System.out.println(output);
            }

            if (!error.isEmpty()) {
                System.out.println("=== 错误输出 ===");
                System.err.println(error);
            }

            // 获取退出状态（Shell 通道未关闭时可能为 null）
            Integer exitStatus = channel.getExitStatus();
            System.out.println("=== 退出状态: " + exitStatus + " ===");
            System.out.println();

            // 手动关闭通道
            System.out.println("手动关闭 Shell 通道...");
            channel.close();

        } catch (Exception e) {
            System.err.println("❌ Shell 通道执行失败: " + e.getMessage());
            throw new IOException("Shell 通道执行失败", e);
        }
    }

    /**
     * 测试 SSH Shell 通道（真正的实时交互）
     *
     * <p>测试场景：</p>
     * <ul>
     *   <li>1. 使用 PipedInputStream/PipedOutputStream 实现实时交互</li>
     *   <li>2. 在单独的线程中动态发送命令</li>
     *   <li>3. 实时读取命令输出</li>
     *   <li>4. 支持根据输出结果决定下一步操作</li>
     * </ul>
     *
     * <p>环境变量配置：</p>
     * <pre>
     * SSH_HOST=your-ssh-host
     * SSH_PORT=22
     * SSH_USERNAME=your-username
     * SSH_PASSWORD=your-password
     * </pre>
     */
    @Test
    public void testSshShellChannelRealTime() throws IOException {
        // 1. 从环境变量加载 SSH 配置
        String sshHost = System.getProperty("SSH_HOST");
        int sshPort = Integer.parseInt(System.getProperty("SSH_PORT", "22"));
        String sshUsername = System.getProperty("SSH_USERNAME");
        String sshPassword = System.getProperty("SSH_PASSWORD");

        // 创建 SSH 客户端
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        try {
            // 连接到 SSH 服务器
            System.out.println("正在连接到 SSH 服务器...");
            ClientSession session = client.connect(sshUsername, sshHost, sshPort)
                    .verify(10, TimeUnit.SECONDS)
                    .getSession();

            // 使用密码认证
            session.addPasswordIdentity(sshPassword);
            session.auth().verify(10, TimeUnit.SECONDS);
            System.out.println("✅ SSH 连接成功！");
            System.out.println();

            // 执行实时交互 Shell 测试
            executeShellCommandsRealTime(session);

            // 关闭会话
            session.close();
            System.out.println("✅ SSH 会话已关闭");

        } catch (Exception e) {
            System.err.println("❌ SSH 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止客户端
            client.stop();
        }
    }

    /**
     * 使用 Shell 通道执行实时交互命令
     * 使用 PipedInputStream/PipedOutputStream 实现真正的实时交互
     *
     * @param session SSH 会话
     * @throws IOException IO 异常
     */
    private void executeShellCommandsRealTime(ClientSession session) throws IOException {
        System.out.println("=== 开始实时交互 Shell 通道测试 ===");
        System.out.println();

        try {
            // 创建管道流（用于实时写入命令）
            PipedOutputStream pipedOut = new PipedOutputStream();
            PipedInputStream pipedIn = new PipedInputStream(pipedOut);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

            ClientChannel channel = session.createShellChannel();

            // 设置输入输出流
            channel.setIn(pipedIn);           // 使用管道输入流
            channel.setOut(outputStream);     // 标准输出
            channel.setErr(errorStream);      // 错误输出

            // 打开 Shell 通道
            channel.open().verify(10, TimeUnit.SECONDS);
            System.out.println("✅ Shell 通道已打开");
            System.out.println();

            // 在单独的线程中发送命令（模拟实时交互）
            Thread commandThread = new Thread(() -> {
                try {
                    System.out.println(">>> 开始发送命令");

                    // 命令1：打印当前目录
                    sendCommand(pipedOut, "pwd");
                    Thread.sleep(500);  // 等待命令执行

                    // 命令2：打印当前用户
                    sendCommand(pipedOut, "whoami");
                    Thread.sleep(500);

                    // 命令3：设置环境变量
                    sendCommand(pipedOut, "export TEST_VAR=HelloWorld");
                    Thread.sleep(500);

                    // 命令4：读取环境变量（验证环境共享）
                    sendCommand(pipedOut, "echo $TEST_VAR");
                    Thread.sleep(500);

                    // 命令5：切换目录
                    sendCommand(pipedOut, "cd /tmp");
                    Thread.sleep(500);

                    // 命令6：再次打印目录（验证目录切换生效）
                    sendCommand(pipedOut, "pwd");
                    Thread.sleep(500);

                    // 命令7：列出文件
                    sendCommand(pipedOut, "ls -la | head -5");
                    Thread.sleep(500);

                    // 命令8：退出 Shell
                    sendCommand(pipedOut, "exit");

                    // 关闭输出流
                    pipedOut.close();

                } catch (Exception e) {
                    System.err.println("❌ 发送命令失败: " + e.getMessage());
                }
            });

            // 启动命令发送线程
            commandThread.start();

            // 等待 Shell 通道关闭（命令执行完成）
            System.out.println("等待命令执行完成...");
            channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), TimeUnit.SECONDS.toMillis(30));

            // 等待命令线程结束
            commandThread.join(5000);

            // 获取所有输出
            String output = outputStream.toString(StandardCharsets.UTF_8);
            String error = errorStream.toString(StandardCharsets.UTF_8);

            // 打印输出
            System.out.println("=== Shell 输出 ===");
            if (!output.isEmpty()) {
                System.out.println(output);
            }

            if (!error.isEmpty()) {
                System.out.println("=== 错误输出 ===");
                System.err.println(error);
            }

            // 获取退出状态
            Integer exitStatus = channel.getExitStatus();
            System.out.println("=== 退出状态: " + exitStatus + " ===");
            System.out.println();

            // 关闭资源
            channel.close();
            outputStream.close();
            errorStream.close();

        } catch (Exception e) {
            System.err.println("❌ 实时交互 Shell 通道执行失败: " + e.getMessage());
            throw new IOException("实时交互 Shell 通道执行失败", e);
        }
    }

    /**
     * 发送命令到 Shell
     *
     * @param outputStream 输出流
     * @param command      命令
     * @throws IOException IO 异常
     */
    private void sendCommand(OutputStream outputStream, String command) throws IOException {
        System.out.println("    发送命令: " + command);
        outputStream.write((command + "\n").getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

}