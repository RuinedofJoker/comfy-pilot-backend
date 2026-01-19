package org.joker.comfypilot.model.domain.model;

import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.AbstractModelTemplate;
import org.joker.comfypilot.model.domain.service.ModelTemplate;

import java.util.Map;

// TODO 基于python sentence_transformers 封装模型，配置里提供python位置，还有SentenceTransformer库调用model.encode时要传的参数
public class SentenceTransformersEmbeddingModelTemplate extends AbstractModelTemplate implements ModelTemplate {

    public SentenceTransformersEmbeddingModelTemplate(AiModel aiModel) {
        super(aiModel);
    }

    @Override
    public ModelCallingType callingType() {
        return ModelCallingType.SENTENCE_TRANSFORMERS_EMBEDDING;
    }

    @Override
    public Map<String, Object> configFormat() {
        return Map.of(
                "python", "本地调用sentence_transformers库的python位置"
        );
    }

}
