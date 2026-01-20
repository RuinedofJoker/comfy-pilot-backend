package org.joker.comfypilot.agent.application.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.converter.AgentConfigDTOConverter;
import org.joker.comfypilot.agent.application.converter.AgentRuntimeConfigDTOConverter;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentRuntimeConfigDTO;
import org.joker.comfypilot.agent.application.service.AgentConfigService;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.repository.AgentConfigRepository;
import org.joker.comfypilot.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent配置服务实现
 */
@Slf4j
@Service
public class AgentConfigServiceImpl implements AgentConfigService {

    @Autowired
    private AgentConfigRepository agentConfigRepository;
    @Autowired
    private AgentConfigDTOConverter dtoConverter;
    @Autowired
    private AgentRuntimeConfigDTOConverter runtimeDTOConverter;

    @Override
    public List<AgentConfigDTO> getAllAgents() {
        List<AgentConfig> agents = agentConfigRepository.findAll();
        return agents.stream()
                .map(dtoConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentRuntimeConfigDTO> getEnabledRuntimeAgents() {
        List<AgentConfig> agents = agentConfigRepository.findAll();
        return agents.stream()
                .filter(AgentConfig::isEnabled)
                .map(runtimeDTOConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AgentConfigDTO getAgentById(Long id) {
        AgentConfig agent = agentConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agent不存在"));
        return dtoConverter.toDTO(agent);
    }

    @Override
    public AgentRuntimeConfigDTO getRuntimeAgentByCode(String agentCode) {
        AgentConfig agent = agentConfigRepository.findByAgentCode(agentCode)
                .orElseThrow(() -> new BusinessException("Agent不存在: " + agentCode));
        return runtimeDTOConverter.toDTO(agent);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableAgent(Long id) {
        AgentConfig agent = agentConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agent不存在"));

        agent.enable();
        agentConfigRepository.update(agent);

        log.info("启用Agent成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableAgent(Long id) {
        AgentConfig agent = agentConfigRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agent不存在"));

        agent.disable();
        agentConfigRepository.update(agent);

        log.info("禁用Agent成功: id={}", id);
    }
}
