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

}
