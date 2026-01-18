package org.joker.comfypilot.cfsvr.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ComfyUI服务器高级功能配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ComfyUI服务器高级功能配置")
public class ComfyuiServerAdvancedFeaturesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "连接方式类型（LOCAL/SSH）")
    private String connectionType;

    @Schema(description = "SSH连接配置")
    private SshConnectionConfigDTO sshConfig;

    @Schema(description = "服务器操作系统类型")
    private String osType;

    @Schema(description = "工作目录路径")
    private String workingDirectory;

    @Schema(description = "环境初始化脚本")
    private String environmentInitScript;

    @Schema(description = "Python命令路径")
    private String pythonCommand;

    @Schema(description = "ComfyUI目录配置")
    private ComfyuiDirectoryConfigDTO directoryConfig;

    @Schema(description = "最后连接测试时间")
    private LocalDateTime lastConnectionTestTime;

    @Schema(description = "连接状态")
    private String connectionStatus;
}
