package org.joker.comfypilot.model.domain.enums;

import lombok.Getter;
import org.joker.comfypilot.model.domain.model.ApiEmbeddingModelTemplate;
import org.joker.comfypilot.model.domain.model.ApiLLMModelTemplate;
import org.joker.comfypilot.model.domain.model.SentenceTransformersEmbeddingModelTemplate;
import org.joker.comfypilot.model.domain.service.ModelTemplate;

/**
 * 模型调用方式
 */
@Getter
public enum ModelCallingType {

    API_LLM("api_llm", "API调用的LLM模型", ApiLLMModelTemplate.class),

    API_EMBEDDING("api_embedding", "API调用的embedding模型", ApiEmbeddingModelTemplate.class),

    SENTENCE_TRANSFORMERS_EMBEDDING("sentence_transformers_embedding", "本地使用SentenceTransformer调用的embedding模型", SentenceTransformersEmbeddingModelTemplate.class),

    ;

    /**
     * 模型调用方式编码
     */
    private final String code;

    /**
     * 模型类型描述
     */
    private final String description;

    /**
     * 模型调用的实现类
     */
    private final Class<? extends ModelTemplate> modelClass;

    ModelCallingType(String code, String description, Class<? extends ModelTemplate> modelClass) {
        this.code = code;
        this.description = description;
        this.modelClass = modelClass;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举值
     * @throws IllegalArgumentException 如果编码无效
     */
    public static ModelCallingType fromCode(String code) {
        for (ModelCallingType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的模型调用方式: " + code);
    }

}
