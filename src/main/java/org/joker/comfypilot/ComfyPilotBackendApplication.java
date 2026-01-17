package org.joker.comfypilot;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.config.EnvLoader;
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
    }

}
