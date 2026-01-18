package org.joker.comfypilot.agent.infrastructure.registry;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.entity.AgentConfig;
import org.joker.comfypilot.agent.domain.enums.AgentStatus;
import org.joker.comfypilot.agent.domain.repository.AgentConfigRepository;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.agent.domain.service.AgentRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent注册表实现
 * 在应用启动时自动扫描并注册所有Agent实现，并同步到数据库
 */
@Slf4j
@Component
public class AgentRegistryImpl implements AgentRegistry, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private AgentConfigRepository agentConfigRepository;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Agent注册表，key为agentCode，value为Agent实例
     */
    private final Map<String, Agent> agentMap = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 应用启动后自动注册所有Agent并同步到数据库
     */
    @PostConstruct
    public void registerAllAgents() {
        log.info("开始扫描并注册Agent实现...");

        // 从Spring容器中获取所有Agent实现
        Map<String, Agent> agentBeans = applicationContext.getBeansOfType(Agent.class);

        for (Agent agent : agentBeans.values()) {
            String agentCode = agent.getAgentCode();
            if (agentMap.containsKey(agentCode)) {
                log.warn("发现重复的Agent编码: {}, 将被覆盖", agentCode);
            }
            agentMap.put(agentCode, agent);

            // 同步Agent到数据库
            syncAgentToDatabase(agent);

            log.info("注册Agent成功: code={}, name={}, version={}",
                    agentCode, agent.getAgentName(), agent.getVersion());
        }

        log.info("Agent注册完成，共注册{}个Agent", agentMap.size());
    }

    @Override
    public List<Agent> getAllAgents() {
        return new ArrayList<>(agentMap.values());
    }

    @Override
    public Agent getAgentByCode(String agentCode) {
        return agentMap.get(agentCode);
    }

    @Override
    public boolean exists(String agentCode) {
        return agentMap.containsKey(agentCode);
    }

    /**
     * 同步Agent到数据库
     * 根据版本号决定是否更新数据库配置
     */
    private void syncAgentToDatabase(Agent agent) {
        String agentCode = agent.getAgentCode();
        Optional<AgentConfig> existingConfigOpt = agentConfigRepository.findByAgentCode(agentCode);

        if (existingConfigOpt.isEmpty()) {
            // Agent不存在，创建新记录
            createNewAgentConfig(agent);
            log.info("创建新Agent配置: code={}, version={}", agentCode, agent.getVersion());
        } else {
            // Agent已存在，比较版本号
            AgentConfig existingConfig = existingConfigOpt.get();
            String dbVersion = existingConfig.getVersion();
            String codeVersion = agent.getVersion();

            if (compareVersion(codeVersion, dbVersion) > 0) {
                // 代码版本 > 数据库版本，更新数据库
                updateAgentConfig(existingConfig, agent);
                log.info("更新Agent配置: code={}, 版本从 {} 升级到 {}",
                        agentCode, dbVersion, codeVersion);
            } else {
                // 数据库版本 >= 代码版本，保留数据库配置（管理员已修改）
                log.info("保留数据库Agent配置: code={}, 数据库版本={}, 代码版本={}",
                        agentCode, dbVersion, codeVersion);
            }
        }
    }

    /**
     * 创建新的Agent配置
     */
    private void createNewAgentConfig(Agent agent) {
        AgentConfig config = AgentConfig.builder()
                .agentCode(agent.getAgentCode())
                .agentName(agent.getAgentName())
                .description(agent.getDescription())
                .version(agent.getVersion())
                .config(agent.getAgentConfig())
                .agentScopeConfig(agent.getAgentScopeConfig())
                .status(AgentStatus.ENABLED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        agentConfigRepository.save(config);
    }

    /**
     * 更新已存在的Agent配置
     */
    private void updateAgentConfig(AgentConfig existingConfig, Agent agent) {
        existingConfig.setAgentName(agent.getAgentName());
        existingConfig.setDescription(agent.getDescription());
        existingConfig.setVersion(agent.getVersion());
        existingConfig.setConfig(agent.getAgentConfig());
        existingConfig.setAgentScopeConfig(agent.getAgentScopeConfig());
        existingConfig.setUpdateTime(LocalDateTime.now());

        agentConfigRepository.update(existingConfig);
    }

    /**
     * 比较版本号（格式：x.y.z）
     *
     * @param version1 版本1
     * @param version2 版本2
     * @return 如果version1 > version2返回正数，相等返回0，小于返回负数
     */
    private int compareVersion(String version1, String version2) {
        if (version1 == null || version2 == null) {
            throw new IllegalArgumentException("版本号不能为空");
        }

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        // 比较主版本号(x)
        int major1 = Integer.parseInt(parts1[0]);
        int major2 = Integer.parseInt(parts2[0]);
        if (major1 != major2) {
            return major1 - major2;
        }

        // 比较次版本号(y)
        int minor1 = parts1.length > 1 ? Integer.parseInt(parts1[1]) : 0;
        int minor2 = parts2.length > 1 ? Integer.parseInt(parts2[1]) : 0;
        if (minor1 != minor2) {
            return minor1 - minor2;
        }

        // 比较修订版本号(z)
        int patch1 = parts1.length > 2 ? Integer.parseInt(parts1[2]) : 0;
        int patch2 = parts2.length > 2 ? Integer.parseInt(parts2[2]) : 0;
        return patch1 - patch2;
    }
}
