package org.joker.comfypilot.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 默认管理员账户配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "admin.default-account")
public class DefaultAdminProperties {

    /**
     * 是否启用默认管理员账户
     */
    private Boolean enabled = true;

    /**
     * 默认管理员用户名
     */
    private String username = "admin";

    /**
     * 默认管理员密码
     */
    private String password = "admin123";
}
