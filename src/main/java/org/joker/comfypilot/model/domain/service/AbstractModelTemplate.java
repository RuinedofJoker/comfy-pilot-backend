package org.joker.comfypilot.model.domain.service;

import org.joker.comfypilot.model.domain.entity.AiModel;

public abstract class AbstractModelTemplate implements ModelTemplate {

    private final AiModel aiModel;

    public AbstractModelTemplate(AiModel aiModel) {
        this.aiModel = aiModel;
    }


    @Override
    public AiModel getAiModel() {
        return this.aiModel;
    }

}
