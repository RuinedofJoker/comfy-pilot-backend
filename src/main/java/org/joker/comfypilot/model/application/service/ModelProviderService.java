package org.joker.comfypilot.model.application.service;

import org.joker.comfypilot.model.application.dto.CreateProviderRequest;
import org.joker.comfypilot.model.application.dto.ModelProviderDTO;
import org.joker.comfypilot.model.application.dto.UpdateProviderRequest;

import java.util.List;

/**
 * 模型提供商服务接口
 */
public interface ModelProviderService {

    /**
     * 创建提供商
     *
     * @param request 创建请求
     * @return 提供商信息
     */
    ModelProviderDTO createProvider(CreateProviderRequest request);

    /**
     * 查询提供商详情
     *
     * @param id 提供商ID
     * @return 提供商信息
     */
    ModelProviderDTO getById(Long id);

    /**
     * 查询提供商列表
     *
     * @return 提供商列表
     */
    List<ModelProviderDTO> listProviders();

    /**
     * 更新提供商
     *
     * @param id 提供商ID
     * @param request 更新请求
     * @return 提供商信息
     */
    ModelProviderDTO updateProvider(Long id, UpdateProviderRequest request);

    /**
     * 删除提供商
     *
     * @param id 提供商ID
     */
    void deleteProvider(Long id);

    /**
     * 启用提供商
     *
     * @param id 提供商ID
     */
    void enableProvider(Long id);

    /**
     * 禁用提供商
     *
     * @param id 提供商ID
     */
    void disableProvider(Long id);
}
