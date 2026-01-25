package org.joker.comfypilot.embedded.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;
import redis.embedded.core.RedisServerBuilder;

import java.io.File;
import java.io.IOException;

@Configuration
@ConditionalOnProperty(value = "embedded.redis", havingValue = "true")
@Slf4j
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Value("${embedded.redis-storage}")
    private String redisStorage;

    @Bean
    public RedisServer redisServer(RedisProperties redisProperties) throws IOException {
        log.info("Starting embedded redis");
        RedisServerBuilder redisServerBuilder = RedisServer.newRedisServer()
                .port(redisProperties.getPort());
        if (StringUtils.isNotBlank(redisStorage)) {
            File redisStorageDir = new File(redisStorage);
            if (redisStorageDir.exists()) {
                if (!redisStorageDir.isDirectory()) {
                    throw new IOException("内嵌Redis持久化文件目录 " + redisStorage + " 已被占用!");
                }
            } else {
                redisStorageDir.mkdirs();
            }

            redisServerBuilder
                    // RDB 满足每60秒1条数据执行刷盘
                    .setting("save 60 1")
                    .setting("dir " + redisStorageDir.getAbsolutePath())
                    .setting("appendonly yes")
                    .setting("appendfilename appendonly.aof")
                    // RDB AOF 混合模式
                    .setting("aof-use-rdb-preamble yes")
                    // AOF 写入策略：每秒刷盘
                    .setting("appendfsync everysec")
            ;
            log.info("Embedded redis use storage, path: {}", redisStorageDir.getAbsolutePath());
        }

        redisServerBuilder
                .setting("bind 0.0.0.0")
                .setting("protected-mode no")
        ;
        if (StringUtils.isBlank(redisProperties.getPassword())) {
            redisProperties.setPassword(null);
        } else {
            redisServerBuilder.setting("requirepass " + redisProperties.getPassword());
        }

        this.redisServer = redisServerBuilder.build();
        try {
            this.redisServer.start();
        } catch (Exception e) {
            log.error("Embedded redis启动失败", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (redisServer != null && redisServer.isActive()) {
                try {
                    redisServer.stop();
                    log.info("Embedded redis server stopped successfully");
                } catch (IOException e) {
                    log.error("Error stopping embedded redis", e);
                }
            }
        }));

        return this.redisServer;
    }

}
