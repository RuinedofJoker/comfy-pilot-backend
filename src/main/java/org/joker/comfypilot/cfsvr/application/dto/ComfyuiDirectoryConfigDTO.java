package org.joker.comfypilot.cfsvr.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ComfyUI目录配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "ComfyUI目录配置")
public class ComfyuiDirectoryConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "ComfyUI安装目录路径")
    private String comfyuiInstallPath;

    @Schema(description = "ComfyUI启动脚本路径")
    private String comfyuiStartupPath;

}
