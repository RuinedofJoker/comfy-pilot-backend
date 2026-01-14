package org.joker.comfypilot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class ComfyPilotBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComfyPilotBackendApplication.class, args);
        log.info("TraceIdInterceptor started");
    }

}
