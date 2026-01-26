package org.joker.comfypilot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joker.comfypilot.common.config.EnvLoader;
import org.joker.comfypilot.common.tool.filesystem.ServerFileSystemTools;
import org.joker.comfypilot.common.util.EmbeddedDatabaseUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
public class ComfyPilotBackendApplication {

    public static void main(String[] args) throws Exception {
        // 在启动 Spring 应用之前加载环境变量
        EnvLoader.load();

        String tempDir = System.getProperty("java.io.tmpdir");
        if (StringUtils.isBlank(tempDir)) {
            log.error("请设置运行时临时目录 java.io.tmpdir");
        }
        log.info("临时目录位置: {}", tempDir);

        ConfigurableApplicationContext applicationContext = SpringApplication.run(ComfyPilotBackendApplication.class, args);
        log.info("ComfyPilot Backend Application started successfully");

        if (!EmbeddedDatabaseUtil.INITIALIZED_EMBEDDED_DATABASE) {
            log.warn("当前使用内嵌H2数据库且未初始化当前使用用户，请去控制台初始化当前使用用户后重启系统");
        }

        ServerFileSystemTools bean = applicationContext.getBean(ServerFileSystemTools.class);
        String tempDirectory = bean.createTempDirectory();
        System.out.println("创建临时目录 " + tempDirectory);
    }

}
