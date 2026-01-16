package org.joker.comfypilot.cfsvr.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.application.converter.ComfyuiServerDTOConverter;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.application.dto.CreateServerRequest;
import org.joker.comfypilot.cfsvr.application.dto.UpdateServerRequest;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerService;
import org.joker.comfypilot.cfsvr.domain.entity.ComfyuiServer;
import org.joker.comfypilot.cfsvr.domain.enums.HealthStatus;
import org.joker.comfypilot.cfsvr.domain.enums.ServerSourceType;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.common.exception.BusinessException;
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
@RequiredArgsConstructor
public class ComfyuiServerServiceImpl implements ComfyuiServerService {

    private final ComfyuiServerRepository repository;
    private final ComfyuiServerDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComfyuiServerDTO createManually(CreateServerRequest request) {
        log.info("手动创建ComfyUI服务, serverName: {}", request.getServerName());

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

        // 构建领域实体
        ComfyuiServer server = ComfyuiServer.builder()
                .serverKey(serverKey)
                .serverName(request.getServerName())
                .description(request.getDescription())
                .baseUrl(request.getBaseUrl())
                .authMode(request.getAuthMode())
                .apiKey(request.getApiKey())
                .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 30)
                .maxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3)
                .sourceType(ServerSourceType.MANUAL)
                .isEnabled(true)
                .healthStatus(HealthStatus.UNKNOWN)
                .build();

        // 保存到数据库
        ComfyuiServer savedServer = repository.save(server);
        log.info("手动创建ComfyUI服务成功, id: {}, serverKey: {}", savedServer.getId(), savedServer.getServerKey());

        return dtoConverter.toDTO(savedServer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComfyuiServerDTO updateServer(Long id, UpdateServerRequest request) {
        log.info("更新ComfyUI服务, id: {}", id);

        // 查询服务
        ComfyuiServer server = repository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 更新基本信息（所有类型都允许）
        server.updateBasicInfo(request.getServerName(), request.getDescription());

        // 更新连接配置（仅MANUAL类型允许，会在领域实体中校验）
        if (request.hasConnectionConfigChanges()) {
            server.updateConnectionConfig(
                    request.getBaseUrl(),
                    request.getAuthMode(),
                    request.getApiKey(),
                    request.getTimeoutSeconds(),
                    request.getMaxRetries()
            );
        }

        // 更新启用状态（仅MANUAL类型允许，会在领域实体中校验）
        if (request.getIsEnabled() != null) {
            server.setEnabled(request.getIsEnabled());
        }

        // 保存更新
        ComfyuiServer updatedServer = repository.save(server);
        log.info("更新ComfyUI服务成功, id: {}", id);

        return dtoConverter.toDTO(updatedServer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteServer(Long id) {
        log.info("删除ComfyUI服务, id: {}", id);

        // 检查服务是否存在
        ComfyuiServer server = repository.findById(id)
                .orElseThrow(() -> new BusinessException("服务不存在"));

        // 代码注册的服务不允许通过管理页面删除
        if (ServerSourceType.CODE_BASED.equals(server.getSourceType())) {
            throw new BusinessException("代码注册的服务不允许通过管理页面删除");
        }

        repository.deleteById(id);
        log.info("删除ComfyUI服务成功, id: {}", id);
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
    public List<ComfyuiServerDTO> listServers(ServerSourceType sourceType, Boolean isEnabled) {
        List<ComfyuiServer> servers;

        if (sourceType != null && isEnabled != null) {
            // 同时按来源类型和启用状态过滤
            servers = repository.findBySourceType(sourceType).stream()
                    .filter(s -> s.getIsEnabled().equals(isEnabled))
                    .collect(Collectors.toList());
        } else if (sourceType != null) {
            // 仅按来源类型过滤
            servers = repository.findBySourceType(sourceType);
        } else if (isEnabled != null) {
            // 仅按启用状态过滤
            servers = repository.findByIsEnabled(isEnabled);
        } else {
            // 查询所有
            servers = repository.findAll();
        }

        return servers.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }
}
