package org.joker.comfypilot.cfsvr.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.AuthMode;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ComfyUI客户端工厂
 * 使用缓存机制避免重复创建WebClient实例
 */
@Slf4j
@Component
public class ComfyUIClientFactory {

    /**
     * 客户端缓存，key为serverId
     * 使用ConcurrentHashMap保证线程安全
     */
    private final ConcurrentHashMap<Long, ComfyUIRestClient> clientCache = new ConcurrentHashMap<>();

    @Autowired
    private ComfyuiServerRepository comfyuiServerRepository;

    /**
     * 根据服务配置创建或获取缓存的REST客户端
     * 使用computeIfAbsent保证线程安全的懒加载
     *
     * @param server ComfyUI服务配置
     * @return REST客户端实例
     */
    public ComfyUIRestClient createRestClient(ComfyuiServer server) {
        return clientCache.computeIfAbsent(server.getId(), id -> {
            log.debug("创建并缓存ComfyUI REST客户端, serverId: {}, serverKey: {}, baseUrl: {}",
                    id, server.getServerKey(), server.getBaseUrl());
            return buildClient(server);
        });
    }

    /**
     * 根据服务器ID创建或获取缓存的REST客户端
     *
     * @param serverId 服务器ID
     * @return REST客户端实例
     * @throws RuntimeException 如果服务器不存在
     */
    public ComfyUIRestClient createRestClient(Long serverId) {
        // 查询服务器
        ComfyuiServer server = comfyuiServerRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("服务器不存在, serverId: " + serverId));
        return createRestClient(server);
    }

    /**
     * 根据服务器标识符创建或获取缓存的REST客户端
     *
     * @param serverKey 服务器唯一标识符
     * @return REST客户端实例
     * @throws RuntimeException 如果服务器不存在
     */
    public ComfyUIRestClient createRestClient(String serverKey) {
        // 查询服务器
        ComfyuiServer server = comfyuiServerRepository.findByServerKey(serverKey)
                .orElseThrow(() -> new RuntimeException("服务器不存在, serverKey: " + serverKey));
        return createRestClient(server);
    }

    /**
     * 构建新的REST客户端实例
     *
     * @param server ComfyUI服务配置
     * @return REST客户端实例
     */
    private ComfyUIRestClient buildClient(ComfyuiServer server) {
        // 创建WebClient.Builder
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(server.getBaseUrl());

        // 根据认证模式配置认证信息
        if (AuthMode.BASIC_AUTH.equals(server.getAuthMode()) && server.getApiKey() != null) {
            log.debug("配置Basic Auth认证, serverId: {}", server.getId());
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, createBasicAuthHeader(server.getApiKey()));
        }

        // 构建WebClient
        WebClient webClient = builder.build();

        // 创建超时时间
        Duration timeout = Duration.ofSeconds(server.getTimeoutSeconds());

        return new ComfyUIRestClientImpl(webClient, timeout);
    }

    /**
     * 使缓存失效（当服务器配置更新时调用）
     *
     * @param serverId 服务器ID
     */
    public void invalidateCache(Long serverId) {
        ComfyUIRestClient removed = clientCache.remove(serverId);
        if (removed != null) {
            log.info("清除客户端缓存, serverId: {}", serverId);
        }
    }

    /**
     * 清空所有缓存
     */
    public void clearAllCache() {
        int size = clientCache.size();
        clientCache.clear();
        log.info("清空所有客户端缓存, 数量: {}", size);
    }

    /**
     * 创建Basic Auth认证头
     *
     * @param apiKey API密钥（格式：username:password）
     * @return Basic Auth认证头值
     */
    private String createBasicAuthHeader(String apiKey) {
        String encoded = Base64.getEncoder()
                .encodeToString(apiKey.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
