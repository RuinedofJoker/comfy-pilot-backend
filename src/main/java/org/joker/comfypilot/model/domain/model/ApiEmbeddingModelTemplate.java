package org.joker.comfypilot.model.domain.model;

import org.joker.comfypilot.model.domain.entity.AiModel;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.AbstractModelTemplate;
import org.joker.comfypilot.model.domain.service.ModelTemplate;

import java.util.Map;

public class ApiEmbeddingModelTemplate extends AbstractModelTemplate implements ModelTemplate {

    public ApiEmbeddingModelTemplate(AiModel aiModel) {
        super(aiModel);
    }

    @Override
    public ModelCallingType callingType() {
        return ModelCallingType.API_EMBEDDING;
    }

    @Override
    public Map<String, Object> configFormat() {
        return Map.of();
    }

}
