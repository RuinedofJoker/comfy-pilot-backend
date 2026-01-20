package org.joker.comfypilot.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.workflow.domain.entity.Workflow;
import org.joker.comfypilot.workflow.domain.repository.WorkflowRepository;
import org.joker.comfypilot.workflow.infrastructure.persistence.converter.WorkflowConverter;
import org.joker.comfypilot.workflow.infrastructure.persistence.mapper.WorkflowMapper;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowPO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工作流仓储实现
 */
@Repository
public class WorkflowRepositoryImpl implements WorkflowRepository {

    @Autowired
    private WorkflowMapper workflowMapper;
    @Autowired
    private WorkflowConverter workflowConverter;

    @Override
    public Optional<Workflow> findById(Long id) {
        WorkflowPO po = workflowMapper.selectById(id);
        return Optional.ofNullable(po).map(workflowConverter::toDomain);
    }

    @Override
    public List<Workflow> findAll() {
        List<WorkflowPO> poList = workflowMapper.selectList(null);
        return poList.stream()
                .map(workflowConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Workflow> findByUserIdAndComfyuiServerId(Long userId, Long comfyuiServerId) {
        LambdaQueryWrapper<WorkflowPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(comfyuiServerId != null, WorkflowPO::getComfyuiServerId, comfyuiServerId)
                .eq(WorkflowPO::getCreateBy, userId);
        List<WorkflowPO> poList = workflowMapper.selectList(wrapper);
        return poList.stream()
                .map(workflowConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Workflow save(Workflow workflow) {
        WorkflowPO po = workflowConverter.toPO(workflow);
        workflowMapper.insertOrUpdate(po);
        return workflowConverter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        workflowMapper.deleteById(id);
    }
}
