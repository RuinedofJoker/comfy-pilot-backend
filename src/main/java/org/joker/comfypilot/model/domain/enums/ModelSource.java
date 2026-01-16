package org.joker.comfypilot.model.domain.enums;

import lombok.Getter;

/**
 * 模型来源枚举
 * 区分模型是通过远程API创建还是代码预定义
 */
@Getter
public enum ModelSource {

    /**
     * 远程API创建
     * 管理员可以通过页面完全管理（创建、修改、删除）
     */
    REMOTE_API("remote_api", "远程API创建"),

    /**
     * 代码预定义
     * 由代码创建，管理员只能编辑基本信息，不能删除
     */
    CODE_DEFINED("code_defined", "代码预定义");

    /**
     * 来源编码
     */
    private final String code;

    /**
     * 来源描述
     */
    private final String description;

    ModelSource(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举值
     * @throws IllegalArgumentException 如果编码无效
     */
    public static ModelSource fromCode(String code) {
        for (ModelSource source : values()) {
            if (source.code.equals(code)) {
                return source;
            }
        }
        throw new IllegalArgumentException("未知的模型来源: " + code);
    }
}
