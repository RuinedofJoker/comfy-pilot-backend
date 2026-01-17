package org.joker.comfypilot.model.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joker.comfypilot.common.domain.BaseEntity;
import org.joker.comfypilot.model.domain.enums.ProviderType;

import java.time.LocalDateTime;

/**
 * 模型提供商领域实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelProvider extends BaseEntity<Long> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 提供商名称
     */
    private String providerName;

    /**
     * 提供协议类型
     */
    private ProviderType providerType;

    /**
     * API基础URL
     */
    private String apiBaseUrl;

    /**
     * API KEY
     */
    private String apiKey;

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
     * 启用提供商
     */
    public void enable() {
        this.isEnabled = true;
    }

    /**
     * 禁用提供商
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
}
