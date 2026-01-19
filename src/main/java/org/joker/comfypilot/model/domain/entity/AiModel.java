package org.joker.comfypilot.model.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.domain.enums.ModelAccessType;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.enums.ModelType;
import org.joker.comfypilot.model.domain.enums.ProviderType;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * AI模型领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiModel extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 模型标识符（唯一）
     */
    private String modelIdentifier;

    /**
     * 接入方式（远程API/本地用命令调用）
     */
    private ModelAccessType accessType;

    /**
     * 模型类型（LLM/Embedding等）
     */
    private ModelType modelType;

    /**
     * 模型调用方式，决定了模型调用使用的Model实现类
     */
    private ModelCallingType modelCallingType;

    /**
     * API基础URL
     */
    private String apiBaseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 提供商ID（远程API时必填）
     */
    private Long providerId;

    /**
     * 提供协议类型
     */
    private ProviderType providerType;

    /**
     * 模型配置（JSON格式）
     */
    private Map<String, Object> modelConfig;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 更新人ID
     */
    private Long updateBy;

    // ==================== 业务方法 ====================

    /**
     * 验证模型配置
     * 远程API接入时必须有providerId
     */
    public void validate() {
        if (ModelAccessType.REMOTE_API.equals(this.accessType)) {
            if (this.providerId == null) {
                throw new BusinessException("远程API接入方式必须指定提供商");
            }
        }
    }

    /**
     * 启用模型
     */
    public void enable() {
        this.isEnabled = true;
    }

    /**
     * 禁用模型
     */
    public void disable() {
        this.isEnabled = false;
    }

    /**
     * 检查是否已启用
     *
     * @return true-已启用，false-已禁用
     */
    public boolean isEnabled() {
        return Boolean.TRUE.equals(this.isEnabled);
    }

    /**
     * 检查是否为远程API接入
     *
     * @return true-远程API，false-本地接入
     */
    public boolean isRemoteApi() {
        return ModelAccessType.REMOTE_API.equals(this.accessType);
    }

    /**
     * 检查是否为本地接入
     *
     * @return true-本地接入，false-远程API
     */
    public boolean isLocal() {
        return ModelAccessType.LOCAL.equals(this.accessType);
    }

    /**
     * 检查是否为LLM模型
     *
     * @return true-LLM模型，false-其他类型
     */
    public boolean isLlm() {
        return ModelType.LLM.equals(this.modelType);
    }

    /**
     * 检查是否为Embedding模型
     *
     * @return true-Embedding模型，false-其他类型
     */
    public boolean isEmbedding() {
        return ModelType.EMBEDDING.equals(this.modelType);
    }
}
