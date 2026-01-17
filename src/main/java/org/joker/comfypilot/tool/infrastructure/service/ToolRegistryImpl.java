package org.joker.comfypilot.tool.infrastructure.service;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.agent.tool.ToolSpecifications;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册中心实现
 * 自动注册所有带 @Tool 注解方法的 Bean
 */
@Slf4j
@Component
public class ToolRegistryImpl implements ToolRegistry, ApplicationContextAware {

    /**
     * 工具实例映射表
     * Key: 工具类的简单类名（如 "WorkflowTool"）
     * Value: 工具实例（Spring Bean）
     */
    private final Map<String, Object> toolMap = new ConcurrentHashMap<>();

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 在所有 Bean 初始化完成后扫描并注册工具
     * 使用 @PostConstruct 避免循环依赖问题
     */
    @PostConstruct
    public void init() {
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

        log.info("工具注册完成，共注册 {} 个工具类", toolCount);
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
            if (method.isAnnotationPresent(Tool.class)) {
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
        String className = toolBean.getClass().getSimpleName();

        // 处理 Spring CGLIB 代理类名（如 "WorkflowTool$$EnhancerBySpringCGLIB$$xxx"）
        if (className.contains("$$")) {
            className = className.substring(0, className.indexOf("$$"));
        }

        toolMap.put(className, toolBean);

        // 统计该工具类中的 @Tool 方法数量
        int methodCount = countToolMethods(toolBean);
        log.info("注册工具: className={}, toolMethods=", className, methodCount);
    }

    /**
     * 统计工具类中 @Tool 方法的数量
     *
     * @param toolBean 工具 Bean 实例
     * @return @Tool 方法数量
     */
    private int countToolMethods(Object toolBean) {
        int count = 0;
        Method[] methods = toolBean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Tool.class)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Object[] getAllTools() {
        return toolMap.values().toArray();
    }

    @Override
    public Object getToolByClassName(String className) {
        return toolMap.get(className);
    }

    @Override
    public List<Object> getToolList() {
        return new ArrayList<>(toolMap.values());
    }

    @Override
    public boolean exists(String className) {
        return toolMap.containsKey(className);
    }

    @Override
    public List<ToolSpecification> getAllToolSpecifications() {
        List<ToolSpecification> allSpecifications = new ArrayList<>();

        // 遍历所有工具实例，提取 ToolSpecification
        for (Object toolBean : toolMap.values()) {
            try {
                List<ToolSpecification> specifications = ToolSpecifications.toolSpecificationsFrom(toolBean);
                allSpecifications.addAll(specifications);
            } catch (Exception e) {
                log.warn("无法从工具实例提取 ToolSpecification: {}, 原因: {}",
                        toolBean.getClass().getSimpleName(), e.getMessage());
            }
        }

        log.debug("提取到 {} 个 ToolSpecification", allSpecifications.size());
        return allSpecifications;
    }

    @Override
    public List<ToolSpecification> getToolSpecificationsByClassName(String className) {
        Object toolBean = toolMap.get(className);

        if (toolBean == null) {
            log.warn("工具类不存在: {}", className);
            return Collections.emptyList();
        }

        try {
            List<ToolSpecification> specifications = ToolSpecifications.toolSpecificationsFrom(toolBean);
            log.debug("从工具类 {} 提取到 {} 个 ToolSpecification", className, specifications.size());
            return specifications;
        } catch (Exception e) {
            log.error("无法从工具类 {} 提取 ToolSpecification: {}", className, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public ToolSpecification getToolSpecificationByMethodName(String className, String methodName) {
        Object toolBean = toolMap.get(className);

        if (toolBean == null) {
            log.warn("工具类不存在: {}", className);
            return null;
        }

        try {
            // 获取工具类的所有方法
            Method[] methods = toolBean.getClass().getDeclaredMethods();

            // 查找匹配的方法
            for (Method method : methods) {
                // 检查方法是否有 @Tool 注解且方法名匹配
                if (method.isAnnotationPresent(Tool.class) && method.getName().equals(methodName)) {
                    // 使用 langchain4j 的 API 从方法提取 ToolSpecification
                    ToolSpecification specification = ToolSpecifications.toolSpecificationFrom(method);
                    log.debug("从工具类 {} 的方法 {} 提取到 ToolSpecification", className, methodName);
                    return specification;
                }
            }

            log.warn("工具类 {} 中未找到方法: {}", className, methodName);
            return null;

        } catch (Exception e) {
            log.error("无法从工具类 {} 的方法 {} 提取 ToolSpecification: {}",
                    className, methodName, e.getMessage());
            return null;
        }
    }
}
