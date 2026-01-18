package org.joker.comfypilot.cfsvr.application.service;

/**
 * ComfyUI服务健康检查服务接口
 */
public interface ComfyuiServerHealthCheckService {

    /**
     * 检查所有启用的服务器健康状态
     */
    void checkAllEnabledServers();

    /**
     * 检查指定服务器的健康状态
     *
     * @param serverId 服务器ID
     */
    void checkServer(Long serverId);
}
