package org.joker.comfypilot.agent.infrastructure.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 待办事项管理工具
 * 用于创建、更新和管理 Agent 执行过程中的待办事项列表
 */
@Slf4j
@Component
@ToolSet("workflow_agent_todo_write_")
public class TodoWriteTool {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 使用 sessionId 作为 key 存储不同会话的待办事项
    private final Map<String, List<TodoItem>> sessionTodos = new ConcurrentHashMap<>();

    /**
     * 待办事项数据结构
     */
    public static class TodoItem {
        private String content;          // 待办事项内容（祈使句形式，如"创建用户模块"）
        private String activeForm;       // 进行中形式（如"正在创建用户模块"）
        private String status;           // 状态: pending, in_progress, completed
        private Long createdAt;          // 创建时间戳
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

        // Getters and Setters
        public String getContent() { return content; }
        public void setContent(String content) {
            this.content = content;
            this.updatedAt = System.currentTimeMillis();
        }

        public String getActiveForm() { return activeForm; }
        public void setActiveForm(String activeForm) {
            this.activeForm = activeForm;
            this.updatedAt = System.currentTimeMillis();
        }

        public String getStatus() { return status; }
        public void setStatus(String status) {
            this.status = status;
            this.updatedAt = System.currentTimeMillis();
        }

        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

        public Long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }

        @Override
        public String toString() {
            return String.format("[%s] %s", status, content);
        }
    }

    /**
     * 创建或更新待办事项列表
     *
     * @param sessionId 会话ID，用于区分不同会话的待办事项
     * @param todosJson 待办事项列表的 JSON 字符串，格式: [{"content":"任务内容","activeForm":"进行中形式","status":"pending|in_progress|completed"}]
     * @param merge 是否合并模式。true: 更新现有列表；false: 替换整个列表
     * @return 操作结果信息
     */
    @Tool("创建或更新待办事项列表。用于跟踪任务执行进度，支持创建新任务、更新任务状态（pending/in_progress/completed）。" +
          "每个待办事项需要包含 content（任务描述，≤14个单词）、activeForm（进行中形式）和 status（状态）。" +
          "merge=true 时更新现有列表，merge=false 时替换整个列表。")
    public String todoWrite(String sessionId, String todosJson, boolean merge) {
        log.info("调用工具: todoWrite, sessionId: {}, merge: {}", sessionId, merge);

        try {
            // 解析 JSON
            @SuppressWarnings("unchecked")
            List<Map<String, String>> todoMaps = OBJECT_MAPPER.readValue(todosJson, List.class);

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
                List<TodoItem> existingTodos = sessionTodos.getOrDefault(sessionId, new ArrayList<>());
                existingTodos.addAll(newTodos);
                sessionTodos.put(sessionId, existingTodos);
            } else {
                // 替换模式：完全替换
                sessionTodos.put(sessionId, newTodos);
            }

            return formatTodoList(sessionId);

        } catch (JsonProcessingException e) {
            log.error("解析待办事项 JSON 失败, sessionId: {}", sessionId, e);
            return "错误: JSON 格式不正确 - " + e.getMessage();
        } catch (Exception e) {
            log.error("更新待办事项失败, sessionId: {}", sessionId, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取当前待办事项列表
     *
     * @param sessionId 会话ID
     * @return 格式化的待办事项列表
     */
    @Tool("获取当前会话的待办事项列表，显示所有任务及其状态")
    public String getTodoList(String sessionId) {
        log.info("调用工具: getTodoList, sessionId: {}", sessionId);
        return formatTodoList(sessionId);
    }

    /**
     * 清空待办事项列表
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Tool("清空当前会话的所有待办事项")
    public String clearTodos(String sessionId) {
        log.info("调用工具: clearTodos, sessionId: {}", sessionId);
        sessionTodos.remove(sessionId);
        return "待办事项列表已清空";
    }

    /**
     * 格式化待办事项列表为可读字符串
     */
    private String formatTodoList(String sessionId) {
        List<TodoItem> todos = sessionTodos.get(sessionId);

        if (todos == null || todos.isEmpty()) {
            return "当前没有待办事项";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("待办事项列表 (共 ").append(todos.size()).append(" 项):\n\n");

        int pendingCount = 0;
        int inProgressCount = 0;
        int completedCount = 0;

        for (int i = 0; i < todos.size(); i++) {
            TodoItem todo = todos.get(i);
            String statusIcon = switch (todo.getStatus()) {
                case "completed" -> "✅";
                case "in_progress" -> "🔄";
                default -> "⏳";
            };

            sb.append(String.format("%d. %s %s\n", i + 1, statusIcon, todo.getContent()));

            switch (todo.getStatus()) {
                case "completed" -> completedCount++;
                case "in_progress" -> inProgressCount++;
                default -> pendingCount++;
            }
        }

        sb.append(String.format("\n统计: ⏳待处理 %d | 🔄进行中 %d | ✅已完成 %d",
                                pendingCount, inProgressCount, completedCount));

        return sb.toString();
    }

    /**
     * 获取原始待办事项数据（供内部使用）
     */
    public List<TodoItem> getRawTodos(String sessionId) {
        return sessionTodos.getOrDefault(sessionId, new ArrayList<>());
    }
}
