package org.joker.comfypilot.comfyui.domain.repository;

import org.joker.comfypilot.comfyui.domain.entity.ComfyuiService;
import org.joker.comfypilot.comfyui.domain.enums.ServiceStatus;

import java.util.List;
import java.util.Optional;

/**
 * ComfyUI服务仓储接口
 */
public interface ComfyuiServiceRepository {

    /**
     * 保存服务
     */
    ComfyuiService save(ComfyuiService service);

    /**
     * 根据ID查询服务
     */
    Optional<ComfyuiService> findById(Long id);

    /**
     * 查询所有服务
     */
    List<ComfyuiService> findAll();

    /**
     * 根据状态查询服务列表
     */
    List<ComfyuiService> findByStatus(ServiceStatus status);

    /**
     * 删除服务
     */
    void deleteById(Long id);
}
