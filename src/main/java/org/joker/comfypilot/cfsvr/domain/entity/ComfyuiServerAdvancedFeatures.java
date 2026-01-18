package org.joker.comfypilot.cfsvr.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.cfsvr.domain.enums.ConnectionType;

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
     * 连接方式类型（SSH/DOCKER/KUBERNETES）
     */
    private ConnectionType connectionType;

    /**
     * SSH连接配置
     */
    private SshConnectionConfig sshConfig;

    /**
     * ComfyUI安装目录路径
     */
    private String comfyuiInstallPath;

    /**
     * 服务器操作系统类型（WINDOWS/LINUX）
     */
    private String osType;

    /**
     * 是否支持插件管理
     */
    private Boolean pluginManagementEnabled;

    /**
     * 插件目录路径
     */
    private String pluginDirectoryPath;

    /**
     * 是否支持模型管理
     */
    private Boolean modelManagementEnabled;

    /**
     * 模型目录路径
     */
    private String modelDirectoryPath;

    /**
     * 是否支持自定义节点管理
     */
    private Boolean customNodeManagementEnabled;

    /**
     * 自定义节点目录路径
     */
    private String customNodeDirectoryPath;

    /**
     * 最后连接测试时间
     */
    private LocalDateTime lastConnectionTestTime;

    /**
     * 连接状态（CONNECTED/DISCONNECTED/UNKNOWN）
     */
    private String connectionStatus;

    // ==================== 业务方法 ====================

    /**
     * 更新SSH连接配置
     *
     * @param sshConfig SSH连接配置对象
     */
    public void updateSshConfig(SshConnectionConfig sshConfig) {
        this.sshConfig = sshConfig;
    }

    /**
     * 更新ComfyUI路径配置
     *
     * @param comfyuiInstallPath      ComfyUI安装目录
     * @param pluginDirectoryPath     插件目录
     * @param modelDirectoryPath      模型目录
     * @param customNodeDirectoryPath 自定义节点目录
     */
    public void updatePathConfig(String comfyuiInstallPath, String pluginDirectoryPath,
                                 String modelDirectoryPath, String customNodeDirectoryPath) {
        this.comfyuiInstallPath = comfyuiInstallPath;
        this.pluginDirectoryPath = pluginDirectoryPath;
        this.modelDirectoryPath = modelDirectoryPath;
        this.customNodeDirectoryPath = customNodeDirectoryPath;
    }

    /**
     * 启用/禁用功能模块
     *
     * @param pluginManagement     是否启用插件管理
     * @param modelManagement      是否启用模型管理
     * @param customNodeManagement 是否启用自定义节点管理
     */
    public void updateFeatureFlags(Boolean pluginManagement, Boolean modelManagement,
                                   Boolean customNodeManagement) {
        this.pluginManagementEnabled = pluginManagement;
        this.modelManagementEnabled = modelManagement;
        this.customNodeManagementEnabled = customNodeManagement;
    }

    /**
     * 更新连接状态
     *
     * @param status 连接状态
     */
    public void updateConnectionStatus(String status) {
        this.connectionStatus = status;
        this.lastConnectionTestTime = LocalDateTime.now();
    }

    /**
     * 判断SSH是否已配置且启用
     *
     * @return true-已配置且启用，false-未配置或未启用
     */
    public boolean isSshConfigured() {
        return this.sshConfig != null && this.sshConfig.isConfigured();
    }

    /**
     * 判断是否支持任何高级功能
     *
     * @return true-支持至少一项高级功能，false-不支持任何高级功能
     */
    public boolean hasAnyFeatureEnabled() {
        return Boolean.TRUE.equals(this.pluginManagementEnabled)
                || Boolean.TRUE.equals(this.modelManagementEnabled)
                || Boolean.TRUE.equals(this.customNodeManagementEnabled);
    }
}
