package org.joker.comfypilot.common.tool.skills;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Skills 配置类
 * 从 application.yml 读取 skills 目录配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "skills")
public class SkillsConfig {

    /**
     * Skills 目录列表
     * 用户可以配置多个目录，每个目录可以包含多个 skills
     */
    private List<String> directories = new ArrayList<>();
}
