package org.joker.comfypilot.cfsvr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SSH连接配置
 * 作为值对象嵌入到 ComfyuiServerAdvancedFeatures 中
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SshConnectionConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否启用SSH连接
     */
    private Boolean enabled;

    /**
     * SSH主机地址
     */
    private String host;

    /**
     * SSH端口
     */
    private Integer port;

    /**
     * SSH用户名
     */
    private String username;

    /**
     * SSH认证方式（PASSWORD/KEY）
     */
    private String authType;

    /**
     * SSH密码（加密存储）
     */
    private String password;

    /**
     * SSH私钥路径
     */
    private String privateKeyPath;

    // ==================== 业务方法 ====================

    /**
     * 更新SSH连接配置
     *
     * @param host           SSH主机地址
     * @param port           SSH端口
     * @param username       SSH用户名
     * @param authType       SSH认证方式
     * @param password       SSH密码
     * @param privateKeyPath SSH私钥路径
     */
    public void updateConfig(String host, Integer port, String username,
                            String authType, String password, String privateKeyPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.authType = authType;
        this.password = password;
        this.privateKeyPath = privateKeyPath;
    }

    /**
     * 判断SSH是否已配置且启用
     *
     * @return true-已配置且启用，false-未配置或未启用
     */
    public boolean isConfigured() {
        return Boolean.TRUE.equals(this.enabled)
                && this.host != null && !this.host.trim().isEmpty()
                && this.port != null
                && this.username != null && !this.username.trim().isEmpty();
    }
}
