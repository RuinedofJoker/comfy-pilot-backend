package org.joker.comfypilot.cfsvr.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 操作系统类型枚举
 */
@Getter
public enum OsType {

    /**
     * Windows操作系统
     */
    WINDOWS("WINDOWS", "Windows"),

    /**
     * Linux操作系统
     */
    LINUX("LINUX", "Linux"),

    /**
     * macOS操作系统
     */
    MACOS("MACOS", "macOS");

    /**
     * 类型码(存储到数据库的值)
     */
    @EnumValue
    @JsonValue
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    OsType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 操作系统类型枚举
     */
    public static OsType fromCode(String code) {
        for (OsType type : OsType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid OS type code: " + code);
    }
}
