package org.joker.comfypilot.agent.infrastructure.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContextHolder;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.RedisUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 状态更新工具
 * 用于记录和报告 Agent 执行过程中的进度状态
 */
@Slf4j
@Component
@ToolSet
@RequiredArgsConstructor
public class StatusUpdateTool {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String REDIS_KEY_PREFIX = "agent:tool:serverTool:status:";
    private static final long REDIS_EXPIRE_HOURS = 24; // 24小时过期

    private final RedisUtil redisUtil;

    /**
     * 状态更新数据结构
     */
    @Getter
    @Setter
    public static class StatusUpdate {
        private String message;          // 状态更新消息（1-3句话）
        private String phase;            // 当前阶段（如: 发现、计划、执行、总结）
        private LocalDateTime timestamp; // 时间戳

        public StatusUpdate() {
            this.timestamp = LocalDateTime.now();
        }

        public StatusUpdate(String message, String phase) {
            this();
            this.message = message;
            this.phase = phase;
        }

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
     * @param message 状态更新消息（1-3句话，包括刚刚发生的事情、即将要做的事情、障碍/风险）
     * @param phase 当前执行阶段（discovery/planning/execution/summary）
     * @return 确认信息
     */
    @Tool(name = "statusUpdate",value = "记录 Agent 执行过程中的状态更新。" +
          "message 应包含: 刚刚发生的事情、即将要做的事情、相关的障碍或风险（1-3句话，对话式风格）。" +
          "phase 表示当前阶段: discovery(发现)、planning(计划)、execution(执行)、summary(总结)。" +
          "应在以下时机调用: 启动时、每个工具批次前后、待办事项更新后、编辑/构建/测试前、完成后、提交前。")
    public String statusUpdate(String message, String phase) {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        String sessionCode = executionContext.getSessionCode();
        log.info("调用工具: statusUpdate, sessionCode: {}, phase: {}", sessionCode, phase);

        try {
            if (message == null || message.trim().isEmpty()) {
                return "错误: 状态更新消息不能为空";
            }

            // 验证 phase 参数
            String normalizedPhase = normalizePhase(phase);

            StatusUpdate update = new StatusUpdate(message.trim(), normalizedPhase);

            // 获取现有历史并添加新记录
            List<StatusUpdate> history = getStatusHistoryFromRedis(sessionCode);
            history.add(update);

            // 限制历史记录数量（保留最近100条）
            if (history.size() > 100) {
                history.removeFirst();
            }

            // 保存到 Redis
            saveStatusHistoryToRedis(sessionCode, history);

            return String.format("✓ 状态已更新 [%s]: %s",
                    normalizedPhase,
                    truncateMessage(message, 50));

        } catch (Exception e) {
            log.error("记录状态更新失败, sessionCode: {}", sessionCode, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取状态更新历史
     *
     * @param limit 返回最近的 N 条记录（默认10条）
     * @return 格式化的状态历史
     */
    @Tool(name = "getStatusHistory",value = "获取当前会话的状态更新历史记录")
    public String getStatusHistory(int limit) {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        String sessionCode = executionContext.getSessionCode();
        log.info("调用工具: getStatusHistory, sessionCode: {}, limit: {}", sessionCode, limit);

        List<StatusUpdate> history = getStatusHistoryFromRedis(sessionCode);

        if (history.isEmpty()) {
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
     * @return 操作结果
     */
    @Tool(name = "clearStatusHistory",value = "清空当前会话的所有状态更新历史")
    public String clearStatusHistory() {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        String sessionCode = executionContext.getSessionCode();
        log.info("调用工具: clearStatusHistory, sessionCode: {}", sessionCode);
        String redisKey = buildRedisKey(sessionCode);
        redisUtil.del(redisKey);
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
    public List<StatusUpdate> getRawStatusHistory(String sessionCode) {
        return getStatusHistoryFromRedis(sessionCode);
    }

    /**
     * 构建 Redis Key
     */
    private String buildRedisKey(String sessionCode) {
        return REDIS_KEY_PREFIX + sessionCode;
    }

    /**
     * 从 Redis 获取状态历史列表
     */
    private List<StatusUpdate> getStatusHistoryFromRedis(String sessionCode) {
        try {
            String redisKey = buildRedisKey(sessionCode);
            Object value = redisUtil.get(redisKey);

            if (value == null) {
                return new ArrayList<>();
            }

            String json = (String) value;
            return JacksonConfig.getObjectMapper().readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("从 Redis 获取状态历史失败, sessionCode: {}", sessionCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * 保存状态历史列表到 Redis
     */
    private void saveStatusHistoryToRedis(String sessionCode, List<StatusUpdate> history) {
        try {
            String redisKey = buildRedisKey(sessionCode);
            String json = JacksonConfig.getObjectMapper().writeValueAsString(history);
            redisUtil.set(redisKey, json, REDIS_EXPIRE_HOURS, TimeUnit.HOURS);
            log.debug("状态历史已保存到 Redis, sessionCode: {}, count: {}", sessionCode, history.size());
        } catch (Exception e) {
            log.error("保存状态历史到 Redis 失败, sessionCode: {}", sessionCode, e);
            throw new RuntimeException("保存状态历史失败", e);
        }
    }
}
