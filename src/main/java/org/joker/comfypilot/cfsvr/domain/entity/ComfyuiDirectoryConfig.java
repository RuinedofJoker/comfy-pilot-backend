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
     * ComfyUI启动脚本路径
     */
    private String comfyuiStartupPath;

}
