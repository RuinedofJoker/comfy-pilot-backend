package org.joker.comfypilot.workflow.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.workflow.application.converter.WorkflowVersionDTOConverter;
import org.joker.comfypilot.workflow.application.dto.CreateVersionRequest;
import org.joker.comfypilot.workflow.application.dto.WorkflowVersionDTO;
import org.joker.comfypilot.workflow.application.service.WorkflowVersionService;
import org.joker.comfypilot.workflow.domain.entity.Workflow;
import org.joker.comfypilot.workflow.domain.entity.WorkflowVersion;
import org.joker.comfypilot.workflow.domain.repository.WorkflowRepository;
import org.joker.comfypilot.workflow.domain.repository.WorkflowVersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工作流版本应用服务实现类
 */
@Slf4j
@Service
public class WorkflowVersionServiceImpl implements WorkflowVersionService {

    @Autowired
    private WorkflowVersionRepository versionRepository;
    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
    private WorkflowVersionDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowVersionDTO createVersion(Long workflowId, CreateVersionRequest request, Long userId, String fromVersionCode) {
        log.info("创建工作流版本, workflowId: {}, userId: {}", workflowId, userId);

        // 验证工作流是否存在
        Workflow workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 计算内容哈希值
        String contentHash = Workflow.calculateContentHash(request.getContent());
        log.info("计算内容哈希值: {}", contentHash);

        // 版本去重：先通过哈希值快速查找
        Optional<WorkflowVersion> existingVersionOpt =
                versionRepository.findByWorkflowIdAndContentHash(workflowId, contentHash);

        if (existingVersionOpt.isPresent()) {
            WorkflowVersion existingVersion = existingVersionOpt.get();
            // 哈希值相同，再比对完整内容
            WorkflowVersion tempVersion = WorkflowVersion.builder()
                    .content(request.getContent())
                    .contentHash(contentHash)
                    .build();

            if (existingVersion.isSameContent(tempVersion)) {
                log.info("发现相同内容的版本，复用已有版本, versionId: {}, versionCode: {}",
                        existingVersion.getId(), existingVersion.getVersionCode());
                return dtoConverter.toDTO(existingVersion);
            }
        }

        // 获取下一个版本号
        String nextVersionCode = UUID.randomUUID().toString();
        log.info("生成新版本号: {}", nextVersionCode);

        // 构建新版本实体
        WorkflowVersion newVersion = WorkflowVersion.builder()
                .workflowId(workflowId)
                .versionCode(nextVersionCode)
                .fromVersionCode(fromVersionCode)
                .content(request.getContent())
                .contentHash(contentHash)
                .changeSummary(request.getChangeSummary())
                .sessionId(request.getSessionId())
                .messageId(request.getMessageId())
                .build();

        // 保存版本
        WorkflowVersion savedVersion = versionRepository.save(newVersion);
        log.info("创建工作流版本成功, versionId: {}, versionCode: {}",
                savedVersion.getId(), savedVersion.getVersionCode());

        return dtoConverter.toDTO(savedVersion);
    }

    @Override
    public List<WorkflowVersionDTO> listVersions(Long workflowId) {
        log.info("查询工作流版本列表, workflowId: {}", workflowId);

        // 验证工作流是否存在
        workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 查询版本列表（按创建时间降序）
        List<WorkflowVersion> versions = versionRepository.findByWorkflowId(workflowId);

        return versions.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public WorkflowVersionDTO getVersionByVersionCode(Long workflowId, String versionCode) {
        log.info("查询工作流版本详情, workflowId: {}, versionCode: {}", workflowId, versionCode);

        // 验证工作流是否存在
        workflowRepository.findById(workflowId)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 查询版本
        WorkflowVersion version = versionRepository.findByWorkflowIdAndVersionCode(workflowId, versionCode)
                .orElseThrow(() -> new ResourceNotFoundException("版本不存在"));

        return dtoConverter.toDTO(version);
    }
}
