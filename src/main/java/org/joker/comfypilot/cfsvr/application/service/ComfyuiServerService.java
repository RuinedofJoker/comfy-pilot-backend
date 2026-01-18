package org.joker.comfypilot.cfsvr.application.service;

import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.application.dto.CreateServerRequest;
import org.joker.comfypilot.cfsvr.application.dto.UpdateServerRequest;

import java.util.List;

/**
 * ComfyUI服务应用服务接口
 */
public interface ComfyuiServerService {

    /**
     * 手动创建服务
     *
     * @param request 创建请求
     * @return 服务信息
     */
    ComfyuiServerDTO createManually(CreateServerRequest request);

    /**
     * 更新服务
     *
     * @param id      服务ID
     * @param request 更新请求
     * @return 服务信息
     */
    ComfyuiServerDTO updateServer(Long id, UpdateServerRequest request);

    /**
     * 删除服务
     *
     * @param id 服务ID
     */
    void deleteServer(Long id);

    /**
     * 根据ID查询
     *
     * @param id 服务ID
     * @return 服务信息
     */
    ComfyuiServerDTO getById(Long id);

    /**
     * 根据serverKey查询
     *
     * @param serverKey 服务唯一标识符
     * @return 服务信息
     */
    ComfyuiServerDTO getByServerKey(String serverKey);

    /**
     * 查询服务列表
     *
     * @param isEnabled 是否启用（可选）
     * @return 服务列表
     */
    List<ComfyuiServerDTO> listServers(Boolean isEnabled);
}
