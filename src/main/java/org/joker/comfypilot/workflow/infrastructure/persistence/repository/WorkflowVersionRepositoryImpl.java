package org.joker.comfypilot.workflow.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.joker.comfypilot.workflow.domain.entity.WorkflowVersion;
import org.joker.comfypilot.workflow.domain.repository.WorkflowVersionRepository;
import org.joker.comfypilot.workflow.infrastructure.persistence.converter.WorkflowVersionConverter;
import org.joker.comfypilot.workflow.infrastructure.persistence.mapper.WorkflowVersionMapper;
import org.joker.comfypilot.workflow.infrastructure.persistence.po.WorkflowVersionPO;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工作流版本仓储实现
 */
@Repository
public class WorkflowVersionRepositoryImpl implements WorkflowVersionRepository {

    @Autowired
    private WorkflowVersionMapper versionMapper;
    @Autowired
    private WorkflowVersionConverter versionConverter;

    @Override
    public Optional<WorkflowVersion> findByWorkflowIdAndVersionCode(Long workflowId, String versionCode) {
        WorkflowVersionPO po = versionMapper.selectOne(
                new LambdaQueryWrapper<WorkflowVersionPO>()
                        .eq(WorkflowVersionPO::getWorkflowId, workflowId)
                        .eq(WorkflowVersionPO::getVersionCode, versionCode)
        );
        return Optional.ofNullable(po).map(versionConverter::toDomain);
    }

    @Override
    public List<WorkflowVersion> findByWorkflowId(Long workflowId) {
        LambdaQueryWrapper<WorkflowVersionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowVersionPO::getWorkflowId, workflowId)
                .orderByDesc(WorkflowVersionPO::getCreateTime);
        List<WorkflowVersionPO> poList = versionMapper.selectList(wrapper);
        return poList.stream()
                .map(versionConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<WorkflowVersion> findByWorkflowIdAndContentHash(Long workflowId, String contentHash) {
        LambdaQueryWrapper<WorkflowVersionPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WorkflowVersionPO::getWorkflowId, workflowId)
                .eq(WorkflowVersionPO::getContentHash, contentHash);
        WorkflowVersionPO po = versionMapper.selectOne(wrapper);
        return Optional.ofNullable(po).map(versionConverter::toDomain);
    }

    @Override
    public WorkflowVersion save(WorkflowVersion version) {
        WorkflowVersionPO po = versionConverter.toPO(version);
        versionMapper.insertOrUpdate(po);
        return versionConverter.toDomain(po);
    }

    @Override
    public void deleteById(Long id) {
        versionMapper.deleteById(id);
    }
}
