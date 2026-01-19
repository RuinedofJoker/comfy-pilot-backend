package org.joker.comfypilot.cfsvr.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.application.converter.ComfyuiServerAdvancedFeaturesConverter;
import org.joker.comfypilot.cfsvr.application.converter.ComfyuiServerDTOConverter;
import org.joker.comfypilot.cfsvr.application.converter.ComfyuiServerPublicDTOConverter;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerPublicDTO;
import org.joker.comfypilot.cfsvr.application.dto.CreateServerRequest;
import org.joker.comfypilot.cfsvr.application.dto.UpdateServerRequest;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerService;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.AuthMode;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIClientFactory;
import org.joker.comfypilot.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ComfyUI服务应用服务实现
 */
@Slf4j
@Service
public class ComfyuiServerServiceImpl implements ComfyuiServerService {

    @Autowired
    private ComfyuiServerRepository repository;
    @Autowired
    private ComfyuiServerDTOConverter dtoConverter;
    @Autowired
    private ComfyuiServerPublicDTOConverter publicDtoConverter;
    @Autowired
    private ComfyuiServerAdvancedFeaturesConverter advancedFeaturesConverter;
    @Autowired
    private ComfyUIClientFactory clientFactory;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComfyuiServerDTO createManually(CreateServerRequest request) {
        log.info("创建ComfyUI服务, serverName: {}", request.getServerName());

        // 处理serverKey：如果用户指定则使用，否则自动生成UUID
        String serverKey = request.getServerKey();
        if (serverKey == null || serverKey.trim().isEmpty()) {
            serverKey = UUID.randomUUID().toString();
            log.info("未指定serverKey，自动生成: {}", serverKey);
        } else {
            // 检查serverKey是否已存在
            if (repository.findByServerKey(serverKey).isPresent()) {
                throw new BusinessException("服务标识符已存在: " + serverKey);
            }
        }

        // 转换认证模式
        AuthMode authMode = AuthMode.fromCode(request.getAuthMode());

        // 构建领域实体
        ComfyuiServer server = ComfyuiServer.builder()
                .serverKey(serverKey)
                .serverName(request.getServerName())
                .description(request.getDescription())
                .baseUrl(request.getBaseUrl())
                .authMode(authMode)
                .apiKey(request.getApiKey())
                .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 30)
                .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
                .isEnabled(request.getIsEnabled() != null ? request.getIsEnabled() : true)
                .healthStatus(HealthStatus.UNKNOWN)
                .advancedFeaturesEnabled(request.getAdvancedFeaturesEnabled() != null ? request.getAdvancedFeaturesEnabled() : false)
                .advancedFeatures(request.getAdvancedFeatures() != null ?
                        advancedFeaturesConverter.toEntity(request.getAdvancedFeatures()) : null)
                .build();

        // 保存到数据库
        ComfyuiServer savedServer = repository.save(server);
        log.info("创建ComfyUI服务成功, id: {}, serverKey: {}", savedServer.getId(), savedServer.getServerKey());

        return dtoConverter.toDTO(savedServer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComfyuiServerDTO updateServer(Long id, UpdateServerRequest request) {
        log.info("更新ComfyUI服务, id: {}", id);

        // 查询服务
        ComfyuiServer server = repository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 更新基本信息
        server.updateBasicInfo(request.getServerName(), request.getDescription());

        // 更新连接配置
        boolean connectionConfigChanged = false;
        if (request.hasConnectionConfigChanges()) {
            AuthMode authMode = AuthMode.fromCode(request.getAuthMode());
            server.updateConnectionConfig(
                    request.getBaseUrl(),
                    authMode,
                    request.getApiKey(),
                    request.getTimeoutSeconds(),
                    request.getMaxRetries()
            );
            connectionConfigChanged = true;
        }

        // 更新启用状态
        if (request.getIsEnabled() != null) {
            server.setEnabled(request.getIsEnabled());
        }

        // 更新高级功能配置
        if (request.hasAdvancedFeaturesChanges()) {
            if (request.getAdvancedFeaturesEnabled() != null) {
                server.setAdvancedFeaturesEnabled(request.getAdvancedFeaturesEnabled());
                log.info("更新高级功能启用状态, id: {}, enabled: {}", id, request.getAdvancedFeaturesEnabled());
            }
            if (request.getAdvancedFeatures() != null) {
                // 使用转换器将DTO转换为Entity
                server.updateAdvancedFeatures(advancedFeaturesConverter.toEntity(request.getAdvancedFeatures()));
                log.info("更新高级功能配置, id: {}", id);
            }
        }

        // 保存更新
        ComfyuiServer updatedServer = repository.save(server);

        // 如果连接配置发生变化，清除客户端缓存
        if (connectionConfigChanged) {
            clientFactory.invalidateCache(id);
            log.info("连接配置已更新，清除客户端缓存, id: {}", id);
        }

        log.info("更新ComfyUI服务成功, id: {}", id);

        return dtoConverter.toDTO(updatedServer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(Long id) {
        log.info("删除ComfyUI服务, id: {}", id);

        // 检查服务是否存在
        repository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 删除服务
        repository.deleteById(id);

        // 清除客户端缓存
        clientFactory.invalidateCache(id);
        log.info("删除ComfyUI服务成功并清除缓存, id: {}", id);
    }

    @Override
    public ComfyuiServerDTO getById(Long id) {
        ComfyuiServer server = repository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));
        return dtoConverter.toDTO(server);
    }

    @Override
    public ComfyuiServerDTO getByServerKey(String serverKey) {
        ComfyuiServer server = repository.findByServerKey(serverKey)
                .orElseThrow(() -> new BusinessException("服务不存在"));
        return dtoConverter.toDTO(server);
    }

    @Override
    public List<ComfyuiServerDTO> listServers() {
        // 查询所有服务（后台管理使用）
        List<ComfyuiServer> servers = repository.findAll();
        return servers.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ComfyuiServerPublicDTO> listEnabledServers() {
        // 只查询启用的服务（前台使用）
        List<ComfyuiServer> servers = repository.findByIsEnabled(true);
        return servers.stream()
                .map(publicDtoConverter::toPublicDTO)
                .collect(Collectors.toList());
    }
}
