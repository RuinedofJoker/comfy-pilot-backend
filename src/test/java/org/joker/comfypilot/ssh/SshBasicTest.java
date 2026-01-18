package org.joker.comfypilot.ssh;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.joker.comfypilot.BaseTest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * SSH 基本功能测试类
 * 测试 Apache MINA SSHD 客户端的基本连接和命令执行功能
 */
public class SshBasicTest extends BaseTest {

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

        // 2. 打印配置信息（密码脱敏）
        System.out.println("=== SSH 连接配置 ===");
        System.out.println("Host: " + sshHost);
        System.out.println("Port: " + sshPort);
        System.out.println("Username: " + sshUsername);
        System.out.println("Password: " + (sshPassword != null ? "******" : "null"));
        System.out.println();

        // 3. 检查必需的环境变量
        if (sshHost == null || sshUsername == null || sshPassword == null) {
            System.out.println("⚠️ SSH 环境变量未配置，跳过测试");
            System.out.println("请在 .env 文件中配置：SSH_HOST, SSH_USERNAME, SSH_PASSWORD");
            return;
        }

        // 4. 创建 SSH 客户端
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        try {
            // 5. 连接到 SSH 服务器
            System.out.println("正在连接到 SSH 服务器...");
            ClientSession session = client.connect(sshUsername, sshHost, sshPort)
                    .verify(10, TimeUnit.SECONDS)
                    .getSession();

            // 6. 使用密码认证
            session.addPasswordIdentity(sshPassword);
            session.auth().verify(10, TimeUnit.SECONDS);
            System.out.println("✅ SSH 连接成功！");
            System.out.println();

            // 7. 执行测试命令
            executeCommand(session, "pwd");
            executeCommand(session, "whoami");
            executeCommand(session, "uname -a");

            // 8. 关闭会话
            session.close();
            System.out.println("✅ SSH 会话已关闭");

        } catch (Exception e) {
            System.err.println("❌ SSH 连接失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 9. 停止客户端
            client.stop();
        }
    }

    /**
     * 执行 SSH 命令并打印结果
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
}