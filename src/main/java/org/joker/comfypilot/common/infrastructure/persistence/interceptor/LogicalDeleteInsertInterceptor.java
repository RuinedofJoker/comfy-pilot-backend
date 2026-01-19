package org.joker.comfypilot.common.infrastructure.persistence.interceptor;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.joker.comfypilot.common.infrastructure.persistence.annotation.UniqueKey;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 逻辑删除插入拦截器
 *
 * 功能：在逻辑删除场景下，如果插入操作指定了主键或唯一键，
 * 先检查是否存在逻辑删除的记录，如果存在则物理删除后再插入
 */
@Slf4j
@Component
@Intercepts({
    @Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
    )
})
public class LogicalDeleteInsertInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        // 1. 判断是否为 INSERT 操作
        if (mappedStatement.getSqlCommandType() != SqlCommandType.INSERT) {
            return invocation.proceed();
        }

        // 2. 判断参数对象是否为空
        if (parameter == null) {
            return invocation.proceed();
        }

        // 3. 判断是否有逻辑删除字段
        Field logicDeleteField = findLogicDeleteField(parameter.getClass());
        if (logicDeleteField == null) {
            return invocation.proceed();
        }

        // 4. 提取主键和唯一键
        KeyInfo keyInfo = extractKeyInfo(parameter);
        if (keyInfo.isEmpty()) {
            return invocation.proceed();
        }

        // 5. 查询是否存在逻辑删除的记录
        Executor executor = (Executor) invocation.getTarget();
        Connection connection = executor.getTransaction().getConnection();

        handleLogicalDeletedRecord(connection, parameter, keyInfo, logicDeleteField);

        // 6. 执行原始插入操作
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取属性
    }

    /**
     * 查找逻辑删除字段
     */
    private Field findLogicDeleteField(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(TableLogic.class) || field.getName().equals("isDeleted")) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    /**
     * 提取主键和唯一键信息
     */
    private KeyInfo extractKeyInfo(Object parameter) {
        KeyInfo keyInfo = new KeyInfo();
        Class<?> clazz = parameter.getClass();

        // 遍历所有字段（包括父类）
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            for (Field field : currentClass.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(parameter);
                    if (value == null) {
                        continue;
                    }

                    // 检查主键
                    if (field.isAnnotationPresent(TableId.class)) {
                        keyInfo.setPrimaryKey(field.getName(), value);
                    }

                    // 检查唯一键
                    if (field.isAnnotationPresent(UniqueKey.class)) {
                        UniqueKey uniqueKey = field.getAnnotation(UniqueKey.class);
                        String group = uniqueKey.group();
                        int order = uniqueKey.order();

                        if (group.isEmpty()) {
                            // 单字段唯一键
                            keyInfo.addSingleUniqueKey(field.getName(), value);
                        } else {
                            // 联合唯一键
                            keyInfo.addCompositeUniqueKey(group, field.getName(), value, order);
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.warn("无法访问字段: {}", field.getName(), e);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return keyInfo;
    }

    /**
     * 处理逻辑删除的记录
     */
    private void handleLogicalDeletedRecord(Connection connection, Object parameter,
                                            KeyInfo keyInfo, Field logicDeleteField) throws Exception {
        String tableName = getTableName(parameter.getClass());
        String logicDeleteFieldName = getColumnName(logicDeleteField);

        // 构建查询条件
        QueryCondition condition = buildQueryCondition(keyInfo);
        if (condition == null) {
            return;
        }

        // 查询记录（忽略逻辑删除）
        String querySql = String.format(
            "SELECT * FROM %s WHERE %s",
            tableName,
            condition.getWhereClause()
        );

        log.debug("查询逻辑删除记录 SQL: {}", querySql);

        try (PreparedStatement ps = connection.prepareStatement(querySql)) {
            // 设置参数
            int paramIndex = 1;
            for (Object value : condition.getParams()) {
                ps.setObject(paramIndex++, value);
            }

            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> records = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getObject("id"));
                    record.put(logicDeleteFieldName, rs.getObject(logicDeleteFieldName));
                    records.add(record);
                }

                // 检查查询结果
                if (records.isEmpty()) {
                    // 没有记录，正常插入
                    return;
                }

                if (records.size() > 1) {
                    // 查到多条记录，数据不一致
                    throw new IllegalStateException(
                        String.format("发现多条记录匹配相同的主键/唯一键，数据不一致！表: %s, 条件: %s",
                            tableName, condition.getWhereClause())
                    );
                }

                // 只有一条记录
                Map<String, Object> record = records.get(0);
                Object isDeletedValue = record.get(logicDeleteFieldName);

                if (isDeletedValue != null && !isDeletedValue.equals(0L) && !isDeletedValue.equals(0)) {
                    // 是逻辑删除的记录，物理删除
                    Long recordId = (Long) record.get("id");
                    physicalDelete(connection, tableName, recordId);
                } else {
                    // 是活跃记录，报错
                    throw new IllegalStateException(
                        String.format("记录已存在且未删除，无法插入！表: %s, 条件: %s",
                            tableName, condition.getWhereClause())
                    );
                }
            }
        }
    }

    /**
     * 物理删除记录
     */
    private void physicalDelete(Connection connection, String tableName, Long id) throws Exception {
        String deleteSql = String.format("DELETE FROM %s WHERE id = ?", tableName);
        log.info("物理删除逻辑删除记录: 表={}, id={}", tableName, id);

        try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            log.info("物理删除完成，影响行数: {}", rows);
        }
    }

    /**
     * 获取表名
     */
    private String getTableName(Class<?> clazz) {
        TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        if (tableNameAnnotation != null && !tableNameAnnotation.value().isEmpty()) {
            return tableNameAnnotation.value();
        }
        // 默认使用类名转下划线
        return camelToUnderscore(clazz.getSimpleName().replace("PO", ""));
    }

    /**
     * 获取列名
     */
    private String getColumnName(Field field) {
        // 简化处理：直接使用字段名转下划线
        return camelToUnderscore(field.getName());
    }

    /**
     * 驼峰转下划线
     */
    private String camelToUnderscore(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 构建查询条件
     */
    private QueryCondition buildQueryCondition(KeyInfo keyInfo) {
        // 优先使用主键
        if (keyInfo.hasPrimaryKey()) {
            return new QueryCondition(
                keyInfo.getPrimaryKeyName() + " = ?",
                Collections.singletonList(keyInfo.getPrimaryKeyValue())
            );
        }

        // 使用单字段唯一键
        if (!keyInfo.getSingleUniqueKeys().isEmpty()) {
            Map.Entry<String, Object> entry = keyInfo.getSingleUniqueKeys().entrySet().iterator().next();
            return new QueryCondition(
                camelToUnderscore(entry.getKey()) + " = ?",
                Collections.singletonList(entry.getValue())
            );
        }

        // 使用联合唯一键（选择第一个完整的组）
        for (Map.Entry<String, Map<String, Object>> groupEntry : keyInfo.getCompositeUniqueKeys().entrySet()) {
            Map<String, Object> fields = groupEntry.getValue();
            if (!fields.isEmpty()) {
                List<String> whereParts = new ArrayList<>();
                List<Object> params = new ArrayList<>();

                for (Map.Entry<String, Object> fieldEntry : fields.entrySet()) {
                    whereParts.add(camelToUnderscore(fieldEntry.getKey()) + " = ?");
                    params.add(fieldEntry.getValue());
                }

                return new QueryCondition(String.join(" AND ", whereParts), params);
            }
        }

        return null;
    }

    /**
     * 键信息类
     */
    private static class KeyInfo {
        private String primaryKeyName;
        private Object primaryKeyValue;
        private Map<String, Object> singleUniqueKeys = new HashMap<>();
        private Map<String, Map<String, Object>> compositeUniqueKeys = new HashMap<>();

        public void setPrimaryKey(String name, Object value) {
            this.primaryKeyName = name;
            this.primaryKeyValue = value;
        }

        public void addSingleUniqueKey(String name, Object value) {
            this.singleUniqueKeys.put(name, value);
        }

        public void addCompositeUniqueKey(String group, String fieldName, Object value, int order) {
            compositeUniqueKeys.computeIfAbsent(group, k -> new LinkedHashMap<>())
                .put(fieldName, value);
        }

        public boolean isEmpty() {
            return primaryKeyValue == null && singleUniqueKeys.isEmpty() && compositeUniqueKeys.isEmpty();
        }

        public boolean hasPrimaryKey() {
            return primaryKeyValue != null;
        }

        public String getPrimaryKeyName() {
            return primaryKeyName;
        }

        public Object getPrimaryKeyValue() {
            return primaryKeyValue;
        }

        public Map<String, Object> getSingleUniqueKeys() {
            return singleUniqueKeys;
        }

        public Map<String, Map<String, Object>> getCompositeUniqueKeys() {
            return compositeUniqueKeys;
        }
    }

    /**
     * 查询条件类
     */
    private static class QueryCondition {
        private final String whereClause;
        private final List<Object> params;

        public QueryCondition(String whereClause, List<Object> params) {
            this.whereClause = whereClause;
            this.params = params;
        }

        public String getWhereClause() {
            return whereClause;
        }

        public List<Object> getParams() {
            return params;
        }
    }
}
