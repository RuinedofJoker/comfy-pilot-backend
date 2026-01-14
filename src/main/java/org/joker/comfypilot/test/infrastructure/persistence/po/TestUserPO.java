package org.joker.comfypilot.test.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.joker.comfypilot.common.infrastructure.persistence.po.BasePO;

/**
 * 测试用户表 PO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("test_user")
public class TestUserPO extends BasePO {

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
