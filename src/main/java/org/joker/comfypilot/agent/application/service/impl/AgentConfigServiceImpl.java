package org.joker.comfypilot.agent.application.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.converter.AgentConfigDTOConverter;
import org.joker.comfypilot.agent.application.converter.AgentRuntimeConfigDTOConverter;
import org.joker.comfypilot.agent.application.dto.AgentConfigDTO;
import org.joker.comfypilot.agent.application.dto.AgentRuntimeConfigDTO;
import org.joker.comfypilot.agent.application.service.AgentConfigService;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.enums.AgentConfigType;
import org.joker.comfypilot.agent.domain.repository.AgentConfigRepository;
import org.joker.comfypilot.agent.domain.service.AgentConfigDefinition;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
    @Autowired
    private ObjectMapper objectMapper;

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
    public AgentConfigDTO getAgentByCode(String agentCode) {
        AgentConfig agent = agentConfigRepository.findByAgentCode(agentCode)
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

    @Override
    public Map<String, Object> validateAndParseAgentConfig(AgentConfigDTO agentConfigDTO, String agentConfigJson) {
        // 如果配置JSON为空，返回空Map
        if (!StringUtils.hasText(agentConfigJson)) {
            return new HashMap<>();
        }

        // 解析JSON字符串为Map
        Map<String, Object> configMap;
        try {
            configMap = objectMapper.readValue(agentConfigJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new ValidationException("Agent配置JSON格式错误: " + e.getMessage());
        }

        // 获取配置定义列表
        List<AgentConfigDefinition> definitions = agentConfigDTO.getAgentConfigDefinitions();
        if (definitions == null || definitions.isEmpty()) {
            return configMap;
        }

        // 校验每个配置项
        for (AgentConfigDefinition definition : definitions) {
            String configName = definition.name();
            Object configValue = configMap.get(configName);

            // 校验必填项
            if (Boolean.TRUE.equals(definition.require()) && configValue == null) {
                throw new ValidationException("配置项 [" + configName + "] 是必填项，不能为空");
            }

            // 如果值为null且非必填，跳过后续校验
            if (configValue == null) {
                continue;
            }

            // 根据类型校验配置值
            validateConfigValue(definition, configName, configValue);
        }

        return configMap;
    }

    /**
     * 校验配置值
     *
     * @param definition 配置定义
     * @param configName 配置名
     * @param configValue 配置值
     */
    private void validateConfigValue(AgentConfigDefinition definition, String configName, Object configValue) {
        AgentConfigType type = definition.type();

        switch (type) {
            case STRING:
                validateStringValue(definition, configName, configValue);
                break;
            case TEXT:
                validateTextValue(configName, configValue);
                break;
            case INT:
                validateIntValue(definition, configName, configValue);
                break;
            case FLOAT:
                validateFloatValue(definition, configName, configValue);
                break;
            case BOOLEAN:
                validateBooleanValue(configName, configValue);
                break;
            case MODEL:
                validateModelValue(configName, configValue);
                break;
            default:
                throw new ValidationException("配置项 [" + configName + "] 的类型 [" + type + "] 不支持");
        }
    }

    /**
     * 校验字符串类型值
     */
    private void validateStringValue(AgentConfigDefinition definition, String configName, Object configValue) {
        if (!(configValue instanceof String)) {
            throw new ValidationException("配置项 [" + configName + "] 必须是字符串类型");
        }

        String strValue = (String) configValue;
        String format = definition.format();

        // 如果定义了格式（正则表达式），则进行格式校验
        if (StringUtils.hasText(format)) {
            try {
                Pattern pattern = Pattern.compile(format);
                if (!pattern.matcher(strValue).matches()) {
                    throw new ValidationException("配置项 [" + configName + "] 的值不符合格式要求: " + format);
                }
            } catch (Exception e) {
                throw new ValidationException("配置项 [" + configName + "] 的格式定义错误: " + e.getMessage());
            }
        }
    }

    /**
     * 校验文本类型值
     */
    private void validateTextValue(String configName, Object configValue) {
        if (!(configValue instanceof String)) {
            throw new ValidationException("配置项 [" + configName + "] 必须是字符串类型");
        }
    }

    /**
     * 校验整数类型值
     */
    private void validateIntValue(AgentConfigDefinition definition, String configName, Object configValue) {
        Integer intValue;

        // 尝试转换为整数
        if (configValue instanceof Integer) {
            intValue = (Integer) configValue;
        } else if (configValue instanceof Number) {
            intValue = ((Number) configValue).intValue();
        } else {
            throw new ValidationException("配置项 [" + configName + "] 必须是整数类型");
        }

        // 校验取值范围
        Integer startScope = definition.intStartScope();
        Integer endScope = definition.intEndScope();

        if (startScope != null && intValue < startScope) {
            throw new ValidationException("配置项 [" + configName + "] 的值不能小于 " + startScope);
        }

        if (endScope != null && intValue > endScope) {
            throw new ValidationException("配置项 [" + configName + "] 的值不能大于 " + endScope);
        }
    }

    /**
     * 校验浮点数类型值
     */
    private void validateFloatValue(AgentConfigDefinition definition, String configName, Object configValue) {
        Double doubleValue;

        // 尝试转换为浮点数
        if (configValue instanceof Double) {
            doubleValue = (Double) configValue;
        } else if (configValue instanceof Number) {
            doubleValue = ((Number) configValue).doubleValue();
        } else {
            throw new ValidationException("配置项 [" + configName + "] 必须是浮点数类型");
        }

        // 校验取值范围
        Double startScope = definition.floatStartScope();
        Double endScope = definition.floatEndScope();

        if (startScope != null && doubleValue < startScope) {
            throw new ValidationException("配置项 [" + configName + "] 的值不能小于 " + startScope);
        }

        if (endScope != null && doubleValue > endScope) {
            throw new ValidationException("配置项 [" + configName + "] 的值不能大于 " + endScope);
        }
    }

    /**
     * 校验布尔类型值
     */
    private void validateBooleanValue(String configName, Object configValue) {
        if (!(configValue instanceof Boolean)) {
            throw new ValidationException("配置项 [" + configName + "] 必须是布尔类型");
        }
    }

    /**
     * 校验模型类型值
     */
    private void validateModelValue(String configName, Object configValue) {
        if (!(configValue instanceof String)) {
            throw new ValidationException("配置项 [" + configName + "] 必须是字符串类型（模型标识符）");
        }
    }
}
