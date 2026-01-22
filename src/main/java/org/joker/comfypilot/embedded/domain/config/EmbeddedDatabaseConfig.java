package org.joker.comfypilot.embedded.domain.config;

import jakarta.annotation.PostConstruct;
import org.joker.comfypilot.agent.domain.service.AgentRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@AutoConfigureBefore(AgentRegistry.class)
//@ConditionalOnProperty(prefix = "embedded", name = "database", havingValue = "true")
public class EmbeddedDatabaseConfig {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        System.out.println();
    }


}
