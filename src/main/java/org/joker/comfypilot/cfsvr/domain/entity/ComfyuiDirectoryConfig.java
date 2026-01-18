package org.joker.comfypilot.cfsvr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ComfyUI目录配置
 * 作为值对象，包含所有ComfyUI相关的目录路径配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComfyuiDirectoryConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ComfyUI安装目录路径
     * ComfyUI的根目录，包含main.py等核心文件
     */
    private String comfyuiInstallPath;

    /**
     * 基础目录路径（--base-directory）
     * 包含 models, custom_nodes, input, output, temp, user 等子目录
     */
    private String baseDirectory;

    /**
     * 输出目录路径（--output-directory）
     * 用于存储生成的图片等输出文件，覆盖 base-directory 设置
     */
    private String outputDirectory;

    /**
     * 临时目录路径（--temp-directory）
     * 用于存储临时文件，覆盖 base-directory 设置
     */
    private String tempDirectory;

    /**
     * 输入目录路径（--input-directory）
     * 用于存储输入文件，覆盖 base-directory 设置
     */
    private String inputDirectory;

    /**
     * 用户目录路径（--user-directory）
     * 用于存储用户数据，覆盖 base-directory 设置
     */
    private String userDirectory;

    /**
     * 前端根目录路径（--front-end-root）
     * 前端文件系统路径
     */
    private String frontEndRoot;

    /**
     * 额外模型路径配置文件列表（--extra-model-paths-config）
     * 可以加载一个或多个 extra_model_paths.yaml 文件
     */
    private String extraModelPathsConfig;
}
