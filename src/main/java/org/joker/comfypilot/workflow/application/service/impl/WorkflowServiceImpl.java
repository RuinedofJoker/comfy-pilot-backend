package org.joker.comfypilot.workflow.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.workflow.application.converter.WorkflowDTOConverter;
import org.joker.comfypilot.workflow.application.dto.*;
import org.joker.comfypilot.workflow.application.service.WorkflowService;
import org.joker.comfypilot.workflow.domain.entity.Workflow;
import org.joker.comfypilot.workflow.domain.repository.WorkflowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工作流应用服务实现类
 */
@Slf4j
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Autowired
    private WorkflowRepository workflowRepository;
    @Autowired
    private ComfyuiServerRepository comfyuiServerRepository;
    @Autowired
    private WorkflowDTOConverter dtoConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO createWorkflow(CreateWorkflowRequest request, Long userId) {
        log.info("创建工作流, workflowName: {}, comfyuiServerId: {}, userId: {}",
                request.getWorkflowName(), request.getComfyuiServerId(), userId);

        // 验证ComfyUI服务是否存在
        comfyuiServerRepository.findById(request.getComfyuiServerId())
                .orElseThrow(() -> new ResourceNotFoundException("ComfyUI服务不存在"));

        // 构建领域实体
        Workflow workflow = Workflow.builder()
                .workflowName(request.getWorkflowName())
                .description(request.getDescription())
                .comfyuiServerId(request.getComfyuiServerId())
                .comfyuiServerKey(request.getComfyuiServerKey())
                .isLocked(false)
                .build();

        // 保存到数据库
        Workflow savedWorkflow = workflowRepository.save(workflow);
        log.info("创建工作流成功, id: {}, workflowName: {}", savedWorkflow.getId(), savedWorkflow.getWorkflowName());

        return dtoConverter.toDTO(savedWorkflow);
    }

    @Override
    public WorkflowDTO getById(Long id) {
        log.info("查询工作流详情, id: {}", id);

        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        return dtoConverter.toDTO(workflow);
    }

    @Override
    public List<WorkflowDTO> listWorkflows(Long comfyuiServerId, Boolean isLocked, Long createBy) {
        log.info("查询工作流列表, comfyuiServerId: {}, isLocked: {}, createBy: {}",
                comfyuiServerId, isLocked, createBy);

        List<Workflow> workflows;

        // 根据过滤条件查询
        if (comfyuiServerId != null) {
            workflows = workflowRepository.findByComfyuiServerId(comfyuiServerId);
        } else if (isLocked != null) {
            workflows = workflowRepository.findByIsLocked(isLocked);
        } else if (createBy != null) {
            workflows = workflowRepository.findByCreateBy(createBy);
        } else {
            workflows = workflowRepository.findAll();
        }

        // 应用额外的过滤条件
        if (comfyuiServerId == null && isLocked != null) {
            workflows = workflows.stream()
                    .filter(w -> w.getIsLocked().equals(isLocked))
                    .collect(Collectors.toList());
        }
        if (comfyuiServerId == null && createBy != null) {
            workflows = workflows.stream()
                    .filter(w -> w.getCreateBy().equals(createBy))
                    .collect(Collectors.toList());
        }

        return workflows.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO updateWorkflow(Long id, UpdateWorkflowRequest request, Long userId) {
        log.info("更新工作流, id: {}, userId: {}", id, userId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 检查是否可以编辑
        if (!workflow.canEdit(userId)) {
            throw new BusinessException("工作流已被其他用户锁定，无法编辑");
        }

        // 更新基本信息
        if (request.getWorkflowName() != null) {
            workflow.setWorkflowName(request.getWorkflowName());
        }
        if (request.getDescription() != null) {
            workflow.setDescription(request.getDescription());
        }
        if (request.getThumbnailUrl() != null) {
            workflow.setThumbnailUrl(request.getThumbnailUrl());
        }

        // 保存更新
        Workflow updatedWorkflow = workflowRepository.save(workflow);
        log.info("更新工作流成功, id: {}", id);

        return dtoConverter.toDTO(updatedWorkflow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkflow(Long id, Long userId) {
        log.info("删除工作流, id: {}, userId: {}", id, userId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 检查是否可以编辑
        if (!workflow.canEdit(userId)) {
            throw new BusinessException("工作流已被其他用户锁定，无法删除");
        }

        // 删除工作流
        workflowRepository.deleteById(id);
        log.info("删除工作流成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO saveContent(Long id, SaveWorkflowContentRequest request, Long userId) {
        log.info("保存工作流内容, id: {}, userId: {}", id, userId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 检查是否可以编辑
        if (!workflow.canEdit(userId)) {
            throw new BusinessException("工作流已被其他用户锁定，无法保存内容");
        }

        // 保存内容（领域实体会自动计算哈希值）
        workflow.saveContent(request.getContent());

        // 保存到数据库
        Workflow savedWorkflow = workflowRepository.save(workflow);
        log.info("保存工作流内容成功, id: {}, contentHash: {}", id, savedWorkflow.getActiveContentHash());

        return dtoConverter.toDTO(savedWorkflow);
    }

    @Override
    public String getContent(Long id) {
        log.info("获取工作流内容, id: {}", id);

        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        return workflow.getContent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO lockWorkflow(Long id, Long userId) {
        log.info("锁定工作流, id: {}, userId: {}", id, userId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 锁定工作流（领域实体会检查是否已被锁定）
        workflow.lock(userId);

        // 保存到数据库
        Workflow lockedWorkflow = workflowRepository.save(workflow);
        log.info("锁定工作流成功, id: {}, lockedBy: {}", id, userId);

        return dtoConverter.toDTO(lockedWorkflow);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO unlockWorkflow(Long id, Long userId) {
        log.info("解锁工作流, id: {}, userId: {}", id, userId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        // 检查权限：只有锁定人可以解锁
        if (!workflow.isLockedBy(userId)) {
            throw new BusinessException("只有锁定人可以解锁工作流");
        }

        // 解锁工作流
        workflow.unlock();

        // 保存到数据库
        Workflow unlockedWorkflow = workflowRepository.save(workflow);
        log.info("解锁工作流成功, id: {}", id);

        return dtoConverter.toDTO(unlockedWorkflow);
    }
}
