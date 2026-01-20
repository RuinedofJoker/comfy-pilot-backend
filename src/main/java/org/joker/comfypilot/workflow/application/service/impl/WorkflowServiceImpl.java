package org.joker.comfypilot.workflow.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.cfsvr.domain.repository.ComfyuiServerRepository;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ResourceNotFoundException;
import org.joker.comfypilot.workflow.application.converter.WorkflowDTOConverter;
import org.joker.comfypilot.workflow.application.dto.*;
import org.joker.comfypilot.workflow.application.service.WorkflowLockService;
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
    @Autowired
    private WorkflowLockService workflowLockService;

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
        } else if (createBy != null) {
            workflows = workflowRepository.findByCreateBy(createBy);
        } else {
            workflows = workflowRepository.findAll();
        }

        // 应用额外的过滤条件
        if (createBy != null && comfyuiServerId == null) {
            workflows = workflows.stream()
                    .filter(w -> w.getCreateBy().equals(createBy))
                    .collect(Collectors.toList());
        }

        // 从Redis加载锁定状态并过滤
        List<WorkflowDTO> dtoList = workflows.stream()
                .map(w -> {
                    WorkflowDTO dto = dtoConverter.toDTO(w);
                    // 从Redis加载锁定信息
                    workflowLockService.getLockInfo(w.getId()).ifPresent(lockInfo -> {
                        dto.setLockedByMessageId(lockInfo.getMessageId());
                        dto.setLockedAt(lockInfo.getLockedAt());
                    });
                    return dto;
                })
                .collect(Collectors.toList());

        // 如果需要按锁定状态过滤
        if (isLocked != null) {
            dtoList = dtoList.stream()
                    .filter(dto -> {
                        boolean locked = dto.getLockedByMessageId() != null;
                        return locked == isLocked;
                    })
                    .collect(Collectors.toList());
        }

        return dtoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO updateWorkflow(Long id, UpdateWorkflowRequest request) {
        log.info("更新工作流, id: {}", id);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!workflow.getCreateBy().equals(currentUserId)) {
            throw new BusinessException("工作流不属于当前用户，无法修改");
        }

        // 从Redis检查锁定状态
        if (workflowLockService.isLocked(id)) {
            throw new BusinessException("工作流已被其他消息锁定，无法编辑");
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
    public void deleteWorkflow(Long id, Long messageId) {
        log.info("删除工作流, id: {}, messageId: {}", id, messageId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!workflow.getCreateBy().equals(currentUserId)) {
            throw new BusinessException("工作流不属于当前用户，无法删除");
        }

        // 从Redis检查锁定状态
        if (workflowLockService.isLocked(id) && !workflowLockService.isLockedByMessage(id, messageId)) {
            throw new BusinessException("工作流已被其他消息锁定，无法删除");
        }

        // 删除工作流
        workflowRepository.deleteById(id);
        log.info("删除工作流成功, id: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO saveContent(Long id, SaveWorkflowContentRequest request, Long messageId) {
        log.info("保存工作流内容, id: {}, messageId: {}", id, messageId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!workflow.getCreateBy().equals(currentUserId)) {
            throw new BusinessException("工作流不属于当前用户，无法保存内容");
        }

        // 从Redis检查锁定状态
        if (workflowLockService.isLocked(id) && !workflowLockService.isLockedByMessage(id, messageId)) {
            throw new BusinessException("工作流已被其他消息锁定，无法保存内容");
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

        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!workflow.getCreateBy().equals(currentUserId)) {
            throw new BusinessException("工作流不属于当前用户，无法查看");
        }

        return workflow.getContent();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO lockWorkflow(Long id, Long messageId) {
        log.info("锁定工作流, id: {}, messageId: {}", id, messageId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!workflow.getCreateBy().equals(currentUserId)) {
            throw new BusinessException("工作流不属于当前用户，无法修改");
        }

        // 使用Redis锁定服务锁定工作流
        boolean locked = workflowLockService.lockWorkflow(id, messageId);
        if (!locked) {
            throw new BusinessException("工作流已被锁定");
        }

        log.info("锁定工作流成功, id: {}, messageId: {}", id, messageId);

        // 返回DTO，包含锁定信息
        WorkflowDTO dto = dtoConverter.toDTO(workflow);
        workflowLockService.getLockInfo(id).ifPresent(lockInfo -> {
            dto.setLockedByMessageId(lockInfo.getMessageId());
            dto.setLockedAt(lockInfo.getLockedAt());
        });

        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDTO unlockWorkflow(Long id, Long messageId) {
        log.info("解锁工作流, id: {}, messageId: {}", id, messageId);

        // 查询工作流
        Workflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("工作流不存在"));

        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!workflow.getCreateBy().equals(currentUserId)) {
            throw new BusinessException("工作流不属于当前用户，无法修改");
        }

        // 使用Redis锁定服务解锁工作流
        boolean unlocked = workflowLockService.unlockWorkflow(id, messageId);
        if (!unlocked) {
            throw new BusinessException("解锁失败，只有锁定该工作流的消息才能解锁");
        }

        log.info("解锁工作流成功, id: {}, messageId: {}", id, messageId);

        return dtoConverter.toDTO(workflow);
    }
}
