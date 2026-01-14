package org.joker.comfypilot.test.application.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.application.dto.BaseDTO;

/**
 * 测试用户 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TestUserDTO extends BaseDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 年龄
     */
    private Integer age;
}
