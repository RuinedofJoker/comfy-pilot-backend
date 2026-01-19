package org.joker.comfypilot.common.infrastructure.persistence.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 自定义逻辑删除拦截器
 * 将逻辑删除值从默认的 1 替换为当前时间戳
 */
@Slf4j
public class CustomLogicDeleteInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        // 只处理 UPDATE 操作
        if (ms.getSqlCommandType() != SqlCommandType.UPDATE) {
            return;
        }

        // 获取 BoundSql
        BoundSql boundSql = ms.getBoundSql(parameter);
        String originalSql = boundSql.getSql();

        // 检查是否包含逻辑删除字段
        if (!originalSql.toLowerCase().contains("is_deleted")) {
            return;
        }

        // 检查 SQL 是否包含 is_deleted = ?（预编译参数）
        if (!originalSql.matches("(?i).*is_deleted\\s*=\\s*\\?.*")) {
            return;
        }

        try {
            // 修改参数值
            modifyParameterValue(boundSql, parameter, ms.getConfiguration());
        } catch (Exception e) {
            log.error("修改逻辑删除参数失败", e);
        }
    }

    /**
     * 修改参数值，将 isDeleted 的值从 1 改为当前时间戳
     */
    private void modifyParameterValue(BoundSql boundSql, Object parameter, Configuration configuration) throws Exception {
        // 获取参数映射列表
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        // 查找 isDeleted 参数的位置
        for (int i = 0; i < parameterMappings.size(); i++) {
            ParameterMapping parameterMapping = parameterMappings.get(i);
            String property = parameterMapping.getProperty();

            // 检查是否是 isDeleted 参数
            if ("et.isDeleted".equals(property) || "isDeleted".equals(property)) {
                // 获取当前参数值
                Object parameterObject = boundSql.getParameterObject();
                Object currentValue = getParameterValue(parameterObject, property, configuration);

                // 如果当前值是 1，替换为时间戳
                if (currentValue != null && (currentValue.equals(1) || currentValue.equals(1L))) {
                    long timestamp = System.currentTimeMillis();
                    setParameterValue(parameterObject, property, timestamp);
                    log.debug("逻辑删除参数已修改: {} = {} -> {}", property, currentValue, timestamp);
                }
            }
        }
    }

    /**
     * 获取参数值
     */
    private Object getParameterValue(Object parameterObject, String property, Configuration configuration) throws Exception {
        if (parameterObject == null) {
            return null;
        }

        // 处理 Map 类型参数
        if (parameterObject instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) parameterObject;
            // 尝试多种可能的 key
            if (map.containsKey(property)) {
                return map.get(property);
            }
            // 尝试去掉前缀
            String simpleProperty = property.contains(".") ? property.substring(property.lastIndexOf(".") + 1) : property;
            if (map.containsKey(simpleProperty)) {
                return map.get(simpleProperty);
            }
            // 尝试 et 对象
            if (map.containsKey("et")) {
                Object et = map.get("et");
                return getFieldValue(et, simpleProperty);
            }
            return null;
        }

        // 处理普通对象
        return getFieldValue(parameterObject, property);
    }

    /**
     * 设置参数值
     */
    private void setParameterValue(Object parameterObject, String property, Object value) throws Exception {
        if (parameterObject == null) {
            return;
        }

        // 处理 Map 类型参数
        if (parameterObject instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) parameterObject;

            // 尝试直接设置
            if (map.containsKey(property)) {
                map.put(property, value);
                return;
            }

            // 尝试去掉前缀
            String simpleProperty = property.contains(".") ? property.substring(property.lastIndexOf(".") + 1) : property;
            if (map.containsKey(simpleProperty)) {
                map.put(simpleProperty, value);
                return;
            }

            // 尝试 et 对象
            if (map.containsKey("et")) {
                Object et = map.get("et");
                setFieldValue(et, simpleProperty, value);
            }
            return;
        }

        // 处理普通对象
        setFieldValue(parameterObject, property, value);
    }

    /**
     * 通过反射获取字段值
     */
    private Object getFieldValue(Object obj, String fieldName) throws Exception {
        if (obj == null) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (NoSuchFieldException e) {
                // 继续查找父类
                clazz = clazz.getSuperclass();
            }
        }
        return null;
    }

    /**
     * 通过反射设置字段值
     */
    private void setFieldValue(Object obj, String fieldName, Object value) throws Exception {
        if (obj == null) {
            return;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(obj, value);
                return;
            } catch (NoSuchFieldException e) {
                // 继续查找父类
                clazz = clazz.getSuperclass();
            }
        }
    }
}
