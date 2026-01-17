package org.joker.comfypilot.tool.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.tool.domain.enums.ToolType;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心实现
 * 自动注册所有 Tool Bean
 */
@Slf4j
@Component
public class ToolRegistryImpl implements ToolRegistry, ApplicationContextAware {

    private final Map<ToolType, Tool> toolsByType = new ConcurrentHashMap<>();
    private final Map<String, Tool> toolsByName = new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 自动注册所有 Tool Bean
        Map<String, Tool> tools = applicationContext.getBeansOfType(Tool.class);
        log.info("发现 {} 个工具", tools.size());

        tools.values().forEach(this::register);

        log.info("工具注册完成，共注册 {} 个工具", toolsByType.size());
    }

    @Override
    public void register(Tool tool) {
        toolsByType.put(tool.getType(), tool);
        toolsByName.put(tool.getName(), tool);
        log.info("注册工具: type={}, name={}, description={}",
                tool.getType(), tool.getName(), tool.getDescription());
    }

    @Override
    public Tool getTool(ToolType type) {
        return toolsByType.get(type);
    }

    @Override
    public Tool getTool(String name) {
        return toolsByName.get(name);
    }

    @Override
    public List<Tool> getAllTools() {
        return new ArrayList<>(toolsByType.values());
    }

    @Override
    public boolean exists(ToolType type) {
        return toolsByType.containsKey(type);
    }

    @Override
    public boolean exists(String name) {
        return toolsByName.containsKey(name);
    }
}
