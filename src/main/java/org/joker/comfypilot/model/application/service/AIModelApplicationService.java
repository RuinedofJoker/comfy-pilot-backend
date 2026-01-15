package org.joker.comfypilot.model.application.service;

import lombok.RequiredArgsConstructor;
import org.joker.comfypilot.model.application.dto.AIModelCreateRequest;
import org.joker.comfypilot.model.application.dto.AIModelDTO;
import org.joker.comfypilot.model.application.dto.AIModelUpdateRequest;
import org.joker.comfypilot.model.domain.repository.AIModelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI模型应用服务
 */
@Service
@RequiredArgsConstructor
public class AIModelApplicationService {

    private final AIModelRepository aiModelRepository;

    /**
     * 创建模型
     */
    public AIModelDTO createModel(AIModelCreateRequest request) {
        // TODO: 实现创建逻辑
        return null;
    }

    /**
     * 更新模型
     */
    public AIModelDTO updateModel(Long id, AIModelUpdateRequest request) {
        // TODO: 实现更新逻辑
        return null;
    }

    /**
     * 获取模型
     */
    public AIModelDTO getModel(Long id) {
        // TODO: 实现查询逻辑
        return null;
    }

    /**
     * 获取所有激活的模型
     */
    public List<AIModelDTO> getAllActiveModels() {
        // TODO: 实现查询逻辑
        return List.of();
    }

    /**
     * 激活模型
     */
    public void activateModel(Long id) {
        // TODO: 实现激活逻辑
    }

    /**
     * 停用模型
     */
    public void deactivateModel(Long id) {
        // TODO: 实现停用逻辑
    }

    /**
     * 删除模型
     */
    public void deleteModel(Long id) {
        // TODO: 实现删除逻辑
    }
}
