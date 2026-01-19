package org.joker.comfypilot.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Spring 容器工具类
 * <p>
 * 提供静态方法从 Spring 容器中获取 Bean 实例
 * <p>
 * 使用场景：在非 Spring 管理的类中需要获取 Spring Bean 时使用
 */
@Slf4j
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 实现 ApplicationContextAware 接口的回调方法
     * Spring 容器启动时会自动调用此方法注入 ApplicationContext
     *
     * @param context Spring 应用上下文
     * @throws BeansException Bean 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (SpringContextUtil.applicationContext == null) {
            SpringContextUtil.applicationContext = context;
            log.info("SpringContextUtil 初始化完成");
        }
    }

    /**
     * 获取 ApplicationContext
     *
     * @return Spring 应用上下文
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 通过 Bean 名称获取 Bean
     *
     * @param name Bean 名称
     * @return Bean 实例
     */
    public static Object getBean(String name) {
        checkApplicationContext();
        return applicationContext.getBean(name);
    }

    /**
     * 通过 Class 获取 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   泛型类型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过 Bean 名称和 Class 获取 Bean
     *
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @param <T>   泛型类型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 获取指定类型的所有 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   泛型类型
     * @return Bean 名称和实例的 Map
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        checkApplicationContext();
        return applicationContext.getBeansOfType(clazz);
    }

    /**
     * 判断是否包含指定名称的 Bean
     *
     * @param name Bean 名称
     * @return true-包含，false-不包含
     */
    public static boolean containsBean(String name) {
        checkApplicationContext();
        return applicationContext.containsBean(name);
    }

    /**
     * 判断指定名称的 Bean 是否为单例
     *
     * @param name Bean 名称
     * @return true-单例，false-非单例
     */
    public static boolean isSingleton(String name) {
        checkApplicationContext();
        return applicationContext.isSingleton(name);
    }

    /**
     * 获取指定名称 Bean 的类型
     *
     * @param name Bean 名称
     * @return Bean 类型
     */
    public static Class<?> getType(String name) {
        checkApplicationContext();
        return applicationContext.getType(name);
    }

    /**
     * 获取指定名称 Bean 的所有别名
     *
     * @param name Bean 名称
     * @return Bean 别名数组
     */
    public static String[] getAliases(String name) {
        checkApplicationContext();
        return applicationContext.getAliases(name);
    }

    /**
     * 检查 ApplicationContext 是否已初始化
     *
     * @throws IllegalStateException 如果 ApplicationContext 未初始化
     */
    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException(
                    "ApplicationContext 未初始化，请确保 SpringContextUtil 已被 Spring 容器管理");
        }
    }
}
