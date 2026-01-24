package org.joker.comfypilot.agent.infrastructure.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 状态更新工具
 * 用于记录和报告 Agent 执行过程中的进度状态
 */
@Slf4j
@Component
@ToolSet
public class StatusUpdateTool {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    // 使用 sessionId 作为 key 存储不同会话的状态更新历史
    private final Map<String, List<StatusUpdate>> sessionStatusHistory = new ConcurrentHashMap<>();

    /**
     * 状态更新数据结构
     */
    public static class StatusUpdate {
        private String message;          // 状态更新消息（1-3句话）
        private String phase;            // 当前阶段（如: 发现、计划、执行、总结）
        private LocalDateTime timestamp; // 时间戳

        public StatusUpdate(String message, String phase) {
            this.message = message;
            this.phase = phase;
            this.timestamp = LocalDateTime.now();
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getPhase() { return phase; }
        public void setPhase(String phase) { this.phase = phase; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] [%s] %s",
                    timestamp.format(TIME_FORMATTER),
                    phase,
                    message);
        }
    }

    /**
     * 记录状态更新
     *
     * @param sessionId 会话ID
     * @param message 状态更新消息（1-3句话，包括刚刚发生的事情、即将要做的事情、障碍/风险）
     * @param phase 当前执行阶段（discovery/planning/execution/summary）
     * @return 确认信息
     */
    @Tool("记录 Agent 执行过程中的状态更新。" +
          "message 应包含: 刚刚发生的事情、即将要做的事情、相关的障碍或风险（1-3句话，对话式风格）。" +
          "phase 表示当前阶段: discovery(发现)、planning(计划)、execution(执行)、summary(总结)。" +
          "应在以下时机调用: 启动时、每个工具批次前后、待办事项更新后、编辑/构建/测试前、完成后、提交前。")
    public String statusUpdate(String sessionId, String message, String phase) {
        log.info("调用工具: statusUpdate, sessionId: {}, phase: {}", sessionId, phase);

        try {
            if (message == null || message.trim().isEmpty()) {
                return "错误: 状态更新消息不能为空";
            }

            // 验证 phase 参数
            String normalizedPhase = normalizePhase(phase);

            StatusUpdate update = new StatusUpdate(message.trim(), normalizedPhase);

            // 获取或创建会话的状态历史
            List<StatusUpdate> history = sessionStatusHistory.computeIfAbsent(
                    sessionId,
                    k -> new ArrayList<>()
            );

            history.add(update);

            // 限制历史记录数量（保留最近100条）
            if (history.size() > 100) {
                history.remove(0);
            }

            return String.format("✓ 状态已更新 [%s]: %s",
                    normalizedPhase,
                    truncateMessage(message, 50));

        } catch (Exception e) {
            log.error("记录状态更新失败, sessionId: {}", sessionId, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取状态更新历史
     *
     * @param sessionId 会话ID
     * @param limit 返回最近的 N 条记录（默认10条）
     * @return 格式化的状态历史
     */
    @Tool("获取当前会话的状态更新历史记录")
    public String getStatusHistory(String sessionId, int limit) {
        log.info("调用工具: getStatusHistory, sessionId: {}, limit: {}", sessionId, limit);

        List<StatusUpdate> history = sessionStatusHistory.get(sessionId);

        if (history == null || history.isEmpty()) {
            return "当前会话没有状态更新历史";
        }

        int actualLimit = Math.min(Math.max(limit, 1), 50); // 限制在 1-50 之间
        int startIndex = Math.max(0, history.size() - actualLimit);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("最近 %d 条状态更新:\n\n", actualLimit));

        for (int i = startIndex; i < history.size(); i++) {
            sb.append(history.get(i).toString()).append("\n");
        }

        return sb.toString();
    }

    /**
     * 清空状态历史
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @Tool("清空当前会话的所有状态更新历史")
    public String clearStatusHistory(String sessionId) {
        log.info("调用工具: clearStatusHistory, sessionId: {}", sessionId);
        sessionStatusHistory.remove(sessionId);
        return "状态更新历史已清空";
    }

    /**
     * 标准化阶段名称
     */
    private String normalizePhase(String phase) {
        if (phase == null || phase.trim().isEmpty()) {
            return "执行中";
        }

        return switch (phase.toLowerCase()) {
            case "discovery", "发现" -> "发现";
            case "planning", "计划" -> "计划";
            case "execution", "执行" -> "执行";
            case "summary", "总结" -> "总结";
            default -> phase;
        };
    }

    /**
     * 截断消息（用于确认信息）
     */
    private String truncateMessage(String message, int maxLength) {
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
    }

    /**
     * 获取原始状态历史（供内部使用）
     */
    public List<StatusUpdate> getRawStatusHistory(String sessionId) {
        return sessionStatusHistory.getOrDefault(sessionId, new ArrayList<>());
    }
}
