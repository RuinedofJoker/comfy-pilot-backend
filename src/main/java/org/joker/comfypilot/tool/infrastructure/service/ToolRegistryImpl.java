package org.joker.comfypilot.tool.infrastructure.service;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心实现
 * 自动注册所有带 @Tool 注解方法的 Bean
 */
@Slf4j
@Component
public class ToolRegistryImpl implements CommandLineRunner, ToolRegistry, ApplicationContextAware {

    /**
     * 工具映射表
     * Key: 工具名
     * Value: 工具对象
     */
    private final Map<String, Tool> toolMap = new ConcurrentHashMap<>();

    /**
     * Class 到 Tool 列表的映射表
     * Key: 工具类的 Class 对象
     * Value: 该类下所有的 Tool 列表
     */
    private final Map<Class<?>, List<Tool>> classToolMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Autowired
    private McpConfigLoader mcpConfigLoader;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 在所有 Bean 初始化完成后扫描并注册工具
     */
    @Override
    public void run(String... args) throws Exception {
        // 获取所有 Spring Bean
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        log.info("开始扫描工具 Bean，共 {} 个 Bean", beanNames.length);

        int toolCount = 0;
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);

                // 检查 Bean 是否包含 @Tool 注解的方法
                if (hasToolMethods(bean)) {
                    registerTool(bean);
                    toolCount++;
                }
            } catch (Exception e) {
                // 跳过无法获取的 Bean
                log.debug("跳过 Bean: {}, 原因: {}", beanName, e.getMessage());
            }
        }

        log.info("本地工具注册完成，共注册 {} 个工具类", toolCount);

        // 注册 MCP 外部工具
        registerMcpTools();
    }

    /**
     * 检查 Bean 是否包含 @Tool 注解的方法
     *
     * @param bean Spring Bean 实例
     * @return true-包含 @Tool 方法，false-不包含
     */
    private boolean hasToolMethods(Object bean) {
        if (bean == null) {
            return false;
        }

        Class<?> clazz = bean.getClass();

        // 跳过 Spring 代理类和系统类
        String className = clazz.getName();
        if (className.contains("$$") || className.startsWith("org.springframework")
                || className.startsWith("java.")) {
            return false;
        }

        // 检查所有方法是否有 @Tool 注解
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 注册工具实例
     *
     * @param toolBean 工具 Bean 实例
     */
    private void registerTool(Object toolBean) {
        Class<?> beanClass = toolBean.getClass();
        String className = beanClass.getSimpleName();

        // 处理 Spring CGLIB 代理类名（如 "WorkflowTool$$EnhancerBySpringCGLIB$$xxx"）
        if (className.contains("$$")) {
            className = className.substring(0, className.indexOf("$$"));
        }

        // 获取实际的类（处理代理类）
        Class<?> actualClass = getActualClass(beanClass);
        List<Tool> classTools = new java.util.ArrayList<>();

        // 获取 @ToolSet 注解的前缀
        String toolPrefix = getToolPrefix(actualClass);

        int methodCount = 0;
        Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(dev.langchain4j.agent.tool.Tool.class)) {
                ToolSpecification toolSpecification = ToolSpecifications.toolSpecificationFrom(method);
                String toolName = toolSpecification.name();

                // 如果有前缀，则添加到 toolName 前面
                if (toolPrefix != null && !toolPrefix.isEmpty()) {
                    toolName = toolPrefix + toolName;
                }

                toolName = Tool.SERVER_TOOL_PREFIX + toolName;

                if (toolMap.containsKey(toolName)) {
                    throw new BusinessException("注册工具出错:工具名:" + toolName + " 重复！");
                }
                Tool tool = new ServerTool(toolName, method, toolBean, toolSpecification);
                toolMap.put(toolName, tool);
                classTools.add(tool);
                methodCount++;
            }
        }

        // 将该类的所有工具添加到 classToolMap
        if (!classTools.isEmpty()) {
            classToolMap.put(actualClass, classTools);
        }

        log.info("注册工具: className={}, toolMethods={}", className, methodCount);
    }

    /**
     * 获取实际的类（处理 Spring 代理类）
     *
     * @param beanClass Bean 的 Class 对象
     * @return 实际的类
     */
    private Class<?> getActualClass(Class<?> beanClass) {
        // 如果是 CGLIB 代理类，获取父类（实际类）
        if (beanClass.getName().contains("$$")) {
            return beanClass.getSuperclass();
        }
        return beanClass;
    }

    /**
     * 获取工具名称前缀
     * 从 @ToolSet 注解中获取 toolPrefix 或 value 属性
     *
     * @param clazz 工具类的 Class 对象
     * @return 工具名称前缀，如果没有注解或前缀为空则返回 null
     */
    private String getToolPrefix(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }

        ToolSet toolSet = clazz.getAnnotation(ToolSet.class);
        if (toolSet == null) {
            return null;
        }

        // 优先使用 toolPrefix，如果为空则使用 value
        String prefix = toolSet.toolPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = toolSet.value();
        }

        return (prefix != null && !prefix.isEmpty()) ? prefix : null;
    }

    @Override
    public boolean exists(String toolName) {
        return toolMap.containsKey(toolName);
    }

    @Override
    public Tool getToolByName(String toolName) {
        return toolMap.get(toolName);
    }

    @Override
    public List<Tool> getToolsByClass(Class<?> clazz) {
        List<Tool> tools = classToolMap.get(clazz);
        return tools != null ? tools : List.of();
    }

    /**
     * 注册 MCP 外部工具
     */
    private void registerMcpTools() {
        try {
            log.info("开始加载 MCP 外部工具");
            List<Tool> mcpTools = mcpConfigLoader.loadMcpTools();

            if (mcpTools.isEmpty()) {
                log.info("未加载到任何 MCP 外部工具");
                return;
            }

            // 注册每个 MCP 工具
            int registeredCount = 0;
            for (Tool tool : mcpTools) {
                String toolName = tool.toolName();

                if (toolMap.containsKey(toolName)) {
                    log.warn("MCP 工具名称冲突，跳过注册: {}", toolName);
                    continue;
                }

                toolMap.put(toolName, tool);
                registeredCount++;
                log.debug("注册 MCP 工具: {}", toolName);
            }

            log.info("MCP 工具注册完成，共注册 {} 个工具", registeredCount);

        } catch (Exception e) {
            log.error("注册 MCP 工具失败: {}", e.getMessage(), e);
        }
    }

}
