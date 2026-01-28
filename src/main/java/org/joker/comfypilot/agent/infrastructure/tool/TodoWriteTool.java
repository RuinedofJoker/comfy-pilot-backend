package org.joker.comfypilot.agent.infrastructure.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContextHolder;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.RedisUtil;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 待办事项管理工具
 * 用于创建、更新和管理 Agent 执行过程中的待办事项列表
 */
@Slf4j
@Component
@ToolSet
public class TodoWriteTool {

    private static final String REDIS_KEY_PREFIX = "agent:tool:serverTool:todo:";
    private static final long REDIS_EXPIRE_HOURS = 24; // 24小时过期

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 待办事项数据结构
     */
    @Getter
    public static class TodoItem {
        // Getters and Setters
        private String content;          // 待办事项内容（祈使句形式，如"创建用户模块"）
        private String activeForm;       // 进行中形式（如"正在创建用户模块"）
        private String status;           // 状态: pending, in_progress, completed
        @Setter
        private Long createdAt;          // 创建时间戳
        @Setter
        private Long updatedAt;          // 更新时间戳

        public TodoItem() {
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;
        }

        public TodoItem(String content, String activeForm, String status) {
            this();
            this.content = content;
            this.activeForm = activeForm;
            this.status = status;
        }

        public void setContent(String content) {
            this.content = content;
            this.updatedAt = System.currentTimeMillis();
        }

        public void setActiveForm(String activeForm) {
            this.activeForm = activeForm;
            this.updatedAt = System.currentTimeMillis();
        }

        public void setStatus(String status) {
            this.status = status;
            this.updatedAt = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            return String.format("[%s] %s", status, content);
        }
    }

    /**
     * 创建或更新待办事项列表
     *
     * @param todosJson 待办事项列表的 JSON 字符串，格式: [{"content":"任务内容","activeForm":"进行中形式","status":"pending|in_progress|completed"}]
     * @param merge     是否合并模式。true: 更新现有列表；false: 替换整个列表
     * @return 操作结果信息
     */
    @Tool(name = "todoWrite",value = "创建或更新待办事项列表，并在用户界面展示该列表。用于跟踪任务执行进度，支持创建新任务、更新任务状态（pending 任务已创建但未执行到/in_progress 任务执行中/completed 任务已完成）。" +
            "每个待办事项需要包含 content（任务描述，≤14个单词）、activeForm（进行中形式）和 status（状态）。" +
            "merge=true 时更新现有列表，merge=false 时替换整个列表。"
    )
    public String todoWrite(@P("待办事项列表的 JSON 字符串，格式: [{\"content\":\"任务内容\",\"activeForm\":\"进行中形式\",\"status\":\"pending|in_progress|completed\"}]") String todosJson, @P("是否合并模式。true: 更新现有列表；false: 替换整个列表") boolean merge) {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        String sessionCode = executionContext.getSessionCode();
        log.info("调用工具: todoWrite, sessionCode: {}, merge: {}", sessionCode, merge);

        try {
            // 解析 JSON
            @SuppressWarnings("unchecked")
            List<Map<String, String>> todoMaps = JacksonConfig.getObjectMapper().readValue(todosJson, List.class);

            List<TodoItem> newTodos = new ArrayList<>();
            for (Map<String, String> todoMap : todoMaps) {
                String content = todoMap.get("content");
                String activeForm = todoMap.get("activeForm");
                String status = todoMap.getOrDefault("status", "pending");

                if (content == null || content.trim().isEmpty()) {
                    continue;
                }

                newTodos.add(new TodoItem(content, activeForm, status));
            }

            if (merge) {
                // 合并模式：更新现有列表
                List<TodoItem> existingTodos = getTodosFromRedis(sessionCode);
                existingTodos.addAll(newTodos);
                saveTodosToRedis(sessionCode, existingTodos);
            } else {
                // 替换模式：完全替换
                saveTodosToRedis(sessionCode, newTodos);
            }

            String formatTodoList = formatTodoList(sessionCode);

            executionContext.getAgentCallback().onPrompt(AgentPromptType.TODO_WRITE, formatTodoList, true);

            return formatTodoList;

        } catch (JsonProcessingException e) {
            log.error("解析待办事项 JSON 失败, sessionId: {}", sessionCode, e);
            return "错误: JSON 格式不正确 - " + e.getMessage();
        } catch (Exception e) {
            log.error("更新待办事项失败, sessionId: {}", sessionCode, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取当前待办事项列表
     *
     * @return 格式化的待办事项列表
     */
    @Tool(name = "getTodoList",value = "获取当前会话的待办事项列表，显示所有任务及其状态")
    public String getTodoList() {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        String sessionCode = executionContext.getSessionCode();
        log.info("调用工具: getTodoList, sessionId: {}", sessionCode);
        return formatTodoList(sessionCode);
    }

    /**
     * 清空待办事项列表
     *
     * @return 操作结果
     */
    @Tool(name = "clearTodos",value = "清空当前会话的所有待办事项")
    public String clearTodos() {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        String sessionCode = executionContext.getSessionCode();
        log.info("调用工具: clearTodos, sessionCode: {}", sessionCode);
        String redisKey = buildRedisKey(sessionCode);
        redisUtil.del(redisKey);
        return "待办事项列表已清空";
    }

    /**
     * 格式化待办事项列表为json字符串
     */
    private String formatTodoList(String sessionId) {
        List<TodoItem> todos = getTodosFromRedis(sessionId);

        if (todos.isEmpty()) {
            return "[]";
        }

        try {
            return JacksonConfig.getObjectMapper().writeValueAsString(todos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取原始待办事项数据（供内部使用）
     */
    public List<TodoItem> getRawTodos(String sessionId) {
        return getTodosFromRedis(sessionId);
    }

    /**
     * 构建 Redis Key
     */
    private String buildRedisKey(String sessionId) {
        return REDIS_KEY_PREFIX + sessionId;
    }

    /**
     * 从 Redis 获取待办事项列表
     */
    private List<TodoItem> getTodosFromRedis(String sessionId) {
        try {
            String redisKey = buildRedisKey(sessionId);
            Object value = redisUtil.get(redisKey);

            if (value == null) {
                return new ArrayList<>();
            }

            String json = (String) value;
            return JacksonConfig.getObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("从 Redis 获取待办事项失败, sessionId: {}", sessionId, e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存待办事项列表到 Redis
     */
    private void saveTodosToRedis(String sessionCode, List<TodoItem> todos) {
        try {
            String redisKey = buildRedisKey(sessionCode);
            String json = JacksonConfig.getObjectMapper().writeValueAsString(todos);
            redisUtil.set(redisKey, json, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("待办事项已保存到 Redis, sessionCode: {}, count: {}", sessionCode, todos.size());
        } catch (Exception e) {
            log.error("保存待办事项到 Redis 失败, sessionCode: {}", sessionCode, e);
            throw new RuntimeException("保存待办事项失败", e);
        }
    }
}
