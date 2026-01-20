package org.joker.comfypilot.cfsvr.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerHealthCheckService;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIClientFactory;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIRestClient;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.SystemStatsResponse;
import org.joker.comfypilot.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ComfyUI服务健康检查服务实现
 */
@Slf4j
@Service
public class ComfyuiServerHealthCheckServiceImpl implements ComfyuiServerHealthCheckService {

    @Autowired
    private ComfyuiServerRepository repository;
    @Autowired
    private ComfyUIClientFactory clientFactory;

    /**
     * 定时检查所有启用的服务器（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000) // 5分钟 = 300000毫秒
    @Override
    public void checkAllEnabledServers() {
        log.info("开始定时健康检查任务");

        List<ComfyuiServer> servers = repository.findByIsEnabled(true);
        log.info("找到 {} 个启用的服务器", servers.size());

        for (ComfyuiServer server : servers) {
            try {
                checkServer(server.getId());
            } catch (Exception e) {
                log.error("检查服务器健康状态失败, serverId: {}, serverKey: {}",
                        server.getId(), server.getServerKey(), e);
            }
        }

        log.info("定时健康检查任务完成");
    }

    @Override
    public void checkServer(Long serverId) {
        log.debug("检查服务器健康状态, serverId: {}", serverId);

        // 查询服务器
        ComfyuiServer server = repository.findById(serverId)
                .orElseThrow(() -> new BusinessException("服务器不存在, serverId: " + serverId));

        HealthStatus healthStatus;
        try {
            // 创建客户端
            ComfyUIRestClient client = clientFactory.createRestClient(server);

            // 调用系统状态接口
            SystemStatsResponse stats = client.getSystemStats();

            // 如果能成功获取系统状态，则认为服务健康
            if (stats != null && stats.getSystem() != null) {
                healthStatus = HealthStatus.HEALTHY;
                log.info("服务器健康检查成功, serverId: {}, serverKey: {}, version: {}",
                        serverId, server.getServerKey(), stats.getSystem().getComfyuiVersion());
            } else {
                healthStatus = HealthStatus.UNHEALTHY;
                log.warn("服务器健康检查失败（响应为空）, serverId: {}, serverKey: {}",
                        serverId, server.getServerKey());
            }
        } catch (Exception e) {
            healthStatus = HealthStatus.UNHEALTHY;
            log.error("服务器健康检查失败, serverId: {}, serverKey: {}",
                    serverId, server.getServerKey(), e);
        }

        // 更新健康状态
        server.updateHealthStatus(healthStatus);
        repository.save(server);

        log.debug("服务器健康状态已更新, serverId: {}, healthStatus: {}", serverId, healthStatus);
    }
}
