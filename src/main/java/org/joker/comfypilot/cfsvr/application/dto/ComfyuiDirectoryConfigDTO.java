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

    @Schema(description = "基础目录路径（--base-directory）")
    private String baseDirectory;

    @Schema(description = "输出目录路径（--output-directory）")
    private String outputDirectory;

    @Schema(description = "临时目录路径（--temp-directory）")
    private String tempDirectory;

    @Schema(description = "输入目录路径（--input-directory）")
    private String inputDirectory;

    @Schema(description = "用户目录路径（--user-directory）")
    private String userDirectory;

    @Schema(description = "前端根目录路径（--front-end-root）")
    private String frontEndRoot;

    @Schema(description = "额外模型路径配置文件列表（--extra-model-paths-config）")
    private String extraModelPathsConfig;
}
