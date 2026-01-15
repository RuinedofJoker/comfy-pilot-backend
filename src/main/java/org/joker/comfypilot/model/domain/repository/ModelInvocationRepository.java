package org.joker.comfypilot.model.domain.repository;

import org.joker.comfypilot.model.domain.entity.ModelInvocation;
import org.joker.comfypilot.model.domain.enums.InvocationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 模型调用记录仓储接口
 */
public interface ModelInvocationRepository {

    /**
     * 保存调用记录
     */
    ModelInvocation save(ModelInvocation invocation);

    /**
     * 根据ID查询
     */
    Optional<ModelInvocation> findById(Long id);

    /**
     * 根据模型ID查询
     */
    List<ModelInvocation> findByModelId(Long modelId);

    /**
     * 根据Agent执行ID查询
     */
    List<ModelInvocation> findByAgentExecutionId(Long agentExecutionId);

    /**
     * 根据状态查询
     */
    List<ModelInvocation> findByStatus(InvocationStatus status);

    /**
     * 查询时间范围内的调用记录
     */
    List<ModelInvocation> findByTimeRange(LocalDateTime startTime, LocalDateTime endTime);
}
