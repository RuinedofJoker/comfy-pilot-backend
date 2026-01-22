package org.joker.comfypilot;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.config.EnvLoader;
import org.joker.comfypilot.common.util.EmbeddedDatabaseUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class ComfyPilotBackendApplication {

    public static void main(String[] args) {
        // 在启动 Spring 应用之前加载环境变量
        EnvLoader.load();

        ConfigurableApplicationContext applicationContext = SpringApplication.run(ComfyPilotBackendApplication.class, args);
        log.info("ComfyPilot Backend Application started successfully");

        if (!EmbeddedDatabaseUtil.INITIALIZED_EMBEDDED_DATABASE) {
            log.warn("当前使用内嵌H2数据库且未初始化当前使用用户，请去控制台初始化当前使用用户后重启系统");
        }
    }

}
