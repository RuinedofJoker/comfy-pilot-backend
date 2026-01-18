package org.joker.comfypilot.cfsvr.domain.repository;

import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;

import java.util.List;
import java.util.Optional;

/**
 * ComfyUI服务仓储接口
 */
public interface ComfyuiServerRepository {

    /**
     * 根据ID查询
     *
     * @param id 服务ID
     * @return 服务实体
     */
    Optional<ComfyuiServer> findById(Long id);

    /**
     * 根据serverKey查询
     *
     * @param serverKey 服务唯一标识符
     * @return 服务实体
     */
    Optional<ComfyuiServer> findByServerKey(String serverKey);

    /**
     * 查询所有服务
     *
     * @return 服务列表
     */
    List<ComfyuiServer> findAll();

    /**
     * 根据启用状态查询
     *
     * @param isEnabled 是否启用
     * @return 服务列表
     */
    List<ComfyuiServer> findByIsEnabled(Boolean isEnabled);

    /**
     * 保存服务
     *
     * @param server 服务实体
     * @return 保存后的服务实体
     */
    ComfyuiServer save(ComfyuiServer server);

    /**
     * 根据ID删除
     *
     * @param id 服务ID
     */
    void deleteById(Long id);
}
