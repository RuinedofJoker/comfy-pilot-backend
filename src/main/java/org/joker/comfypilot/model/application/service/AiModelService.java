package org.joker.comfypilot.model.application.service;

import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.dto.AiModelSimpleDTO;
import org.joker.comfypilot.model.application.dto.CreateModelRequest;
import org.joker.comfypilot.model.application.dto.UpdateModelRequest;

import java.util.List;

/**
 * AI模型服务接口
 */
public interface AiModelService {

    /**
     * 创建模型
     *
     * @param request 创建请求
     * @return 模型信息
     */
    AiModelDTO createModel(CreateModelRequest request);

    /**
     * 查询模型详情
     *
     * @param id 模型ID
     * @return 模型信息
     */
    AiModelDTO getById(Long id);

    /**
     * 查询模型列表
     *
     * @return 模型列表
     */
    List<AiModelDTO> listModels();

    /**
     * 更新模型
     *
     * @param id 模型ID
     * @param request 更新请求
     * @return 模型信息
     */
    AiModelDTO updateModel(Long id, UpdateModelRequest request);

    /**
     * 删除模型
     *
     * @param id 模型ID
     */
    void deleteModel(Long id);

    /**
     * 启用模型
     *
     * @param id 模型ID
     */
    void enableModel(Long id);

    /**
     * 禁用模型
     *
     * @param id 模型ID
     */
    void disableModel(Long id);

    /**
     * 根据模型标识符查询
     *
     * @param modelIdentifier 模型标识符
     * @return 模型信息
     */
    AiModelDTO getByModelIdentifier(String modelIdentifier);

    /**
     * 查询所有启用的模型（用于前台）
     *
     * @return 启用的模型简化信息列表
     */
    List<AiModelSimpleDTO> listEnabledModels();
}
