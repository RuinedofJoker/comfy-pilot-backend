package org.joker.comfypilot.model.domain.service;

import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;

import java.util.Map;

/**
 * 模型模板，提供了不同类型模型的执行方法
 */
public interface ModelTemplate {

    /**
     *
     * @return
     */
    AiModel getAiModel();

    /**
     * 支持的模型调用方式，决定了模型的入参和出参
     *
     * @return 返回模型调用方式
     */
    ModelCallingType callingType();

    /**
     * 配置格式
     *
     * @return 返回模型配置格式
     */
    Map<String, Object> configFormat();

}
