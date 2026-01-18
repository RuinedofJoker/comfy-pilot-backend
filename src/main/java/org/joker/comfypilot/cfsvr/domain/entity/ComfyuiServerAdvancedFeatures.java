package org.joker.comfypilot.cfsvr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.cfsvr.domain.enums.ConnectionStatus;
import org.joker.comfypilot.cfsvr.domain.enums.ConnectionType;
import org.joker.comfypilot.cfsvr.domain.enums.OsType;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ComfyUI服务器高级功能配置
 * 作为值对象嵌入到 ComfyuiServer 中，以 JSON 格式存储
 * 描述通过直接连接服务器实现的高级功能支持情况
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComfyuiServerAdvancedFeatures implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 连接方式类型（LOCAL/SSH）
     */
    private ConnectionType connectionType;

    /**
     * SSH连接配置
     */
    private SshConnectionConfig sshConfig;

    /**
     * 服务器操作系统类型
     */
    private OsType osType;

    /**
     * 工作目录路径
     * SSH连接后的默认工作目录，所有命令将在此目录下执行
     */
    private String workingDirectory;

    /**
     * 环境初始化脚本
     * 连接后执行的初始化脚本，用于激活 conda/pyenv 等虚拟环境
     * 例如: "source ~/anaconda3/bin/activate comfyui" 或 "conda activate comfyui"
     */
    private String environmentInitScript;

    /**
     * Python 命令路径
     * Python 解释器的完整路径或命令名称
     * 例如: "/usr/bin/python3" 或 "python" 或 "python3"
     */
    private String pythonCommand;

    /**
     * ComfyUI目录配置
     * 包含所有ComfyUI相关的目录路径配置
     */
    private ComfyuiDirectoryConfig directoryConfig;

    /**
     * 最后连接测试时间
     */
    private LocalDateTime lastConnectionTestTime;

    /**
     * 连接状态
     */
    private ConnectionStatus connectionStatus;
}
