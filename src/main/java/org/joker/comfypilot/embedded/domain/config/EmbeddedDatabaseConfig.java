package org.joker.comfypilot.embedded.domain.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.service.AgentRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(AgentRegistry.class)
@Slf4j
public class EmbeddedDatabaseConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${embedded.database}")
    private boolean enableEmbeddedDatabase;

    @PostConstruct
    public void init() {
        if (enableEmbeddedDatabase) {
            log.info("开始初始化内嵌H2数据库Schema。。。");
        } else {
            log.info("当前未开启内嵌H2数据库");
        }
    }


}
