package org.joker.comfypilot.cfsvr.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * SSH连接配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SSH连接配置")
public class SshConnectionConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "SSH主机地址")
    private String host;

    @Schema(description = "SSH端口")
    private Integer port;

    @Schema(description = "SSH用户名")
    private String username;

    @Schema(description = "SSH认证方式（PASSWORD/KEY）")
    private String authType;

    @Schema(description = "SSH密码（加密存储）")
    private String password;

    @Schema(description = "SSH私钥内容")
    private String privateKeyContent;
}
