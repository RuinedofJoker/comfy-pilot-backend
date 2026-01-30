package org.joker.comfypilot.agent.domain.agent;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.agent.domain.repository.AgentExecutionLogRepository;
import org.joker.comfypilot.agent.infrastructure.memory.ChatMemoryChatMemoryStore;
import org.joker.comfypilot.common.constant.RedisKeyConstants;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.util.RedisUtil;
import org.joker.comfypilot.common.util.TraceIdUtil;
import org.joker.comfypilot.model.domain.service.ChatModelFactory;
import org.joker.comfypilot.session.application.dto.ChatMessageDTO;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
@Slf4j
public class OrderAgent {

    @Autowired
    private ChatModelFactory chatModelFactory;
    @Autowired
    private ChatSessionService chatSessionService;
    @Autowired
    private ChatMemoryChatMemoryStore chatMemoryChatMemoryStore;
    @Autowired
    private AgentExecutionLogRepository executionLogRepository;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    @Qualifier("agentExecutor")
    private Executor agentExecutor;

    public static final Set<String> ALLOWED_PROMPTS = Set.of("/help", "/clear", "/compact");

    private static final String HELP = """
            这是一个help
            """.trim();

    /**
     * 目前命令都是同步执行
     */
    public void execute(AgentExecutionContext executionContext) {
        Map<String, Object> agentScope = executionContext.getAgentScope();
        String userQueryMessage = agentScope.get("UserQueryMessage").toString();

        AgentCallback agentCallback = executionContext.getAgentCallback();
        agentCallback.onPrompt(AgentPromptType.STARTED, null, false);

        try {
            ChatMessageDTO orderMessage = ChatMessageDTO.builder()
                    .sessionId(executionContext.getSessionId())
                    .sessionCode(executionContext.getSessionCode())
                    .requestId(executionContext.getRequestId())
                    .role(MessageRole.USER_ORDER.name())
                    .content(userQueryMessage)
                    .metadata(new HashMap<>())
                    .build();
            chatSessionService.saveMessage(orderMessage);

            switch (userQueryMessage) {
                case "/help" -> {
                    agentCallback.onPrompt(AgentPromptType.AGENT_MESSAGE_BLOCK, HELP, true);
                    completeOrder(executionContext, true, null);
                }
                case "/clear" -> {
                    chatSessionService.clearSession(executionContext.getSessionCode(), executionContext.getUserId());
                    chatMemoryChatMemoryStore.updateMessages(executionContext.getConnectSessionId(), new ArrayList<>());
                    String tokenUsageRedisKey = RedisKeyConstants.getSessionTokenUsageKey(executionContext.getSessionCode());
                    redisUtil.del(tokenUsageRedisKey);
                    agentCallback.onPrompt(AgentPromptType.CLEAR, null, false);
                    agentCallback.onTokenUsage(agentScope, 0, 0, 0, 0);
                    completeOrder(executionContext, true, null);
                }
                case "/compact" -> {
                    CompletableFuture<ChatResponse> future = summery(executionContext);
                    executionContext.getLastLLMFuture().set(future);
                    future.thenRunAsync(() -> {
                        completeOrder(executionContext, true, null);
                    }, agentExecutor).exceptionallyAsync((e) -> {
                        if (executionContext.isInterrupted()) {
                            agentCallback.onInterrupted();
                        } else {
                            log.error("压缩命令执行失败", e);
                            agentCallback.onPrompt(AgentPromptType.ERROR, e.getMessage(), true);
                        }
                        completeOrder(executionContext, false, e);
                        return null;
                    }, agentExecutor);
                }
            }
        } catch (Exception e) {
            log.error("OrderAgent执行出错", e);
            agentCallback.onPrompt(AgentPromptType.ERROR, e.getMessage(), true);
            completeOrder(executionContext, false, e);
        }
    }

    public CompletableFuture<ChatResponse> summery(AgentExecutionContext executionContext) {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> agentScope = executionContext.getAgentScope();
            AgentCallback agentCallback = executionContext.getAgentCallback();
            Map<String, Object> agentConfig = (Map<String, Object>) agentScope.get("AgentConfig");
            agentCallback.onPrompt(AgentPromptType.SUMMARY, null, false);
            ChatModel chatModel = chatModelFactory.createChatModel(
                    agentConfig.get("llmModelIdentifier").toString(),
                    agentConfig
            );

            if (executionContext.isInterrupted()) {
                throw new BusinessException(new InterruptedException("生成摘要被手动中断"));
            }

            SystemMessage summerySystemMessage = SystemMessage.from(AgentPrompts.SUMMARY_SYSTEM_PROMPT);
            agentCallback.addMemoryMessage(summerySystemMessage, null, null);
            UserMessage summeryUserMessage = UserMessage.from(AgentPrompts.SUMMARY_USER_PROMPT);
            agentCallback.addMemoryMessage(summeryUserMessage, null, null);

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(agentCallback.getMemoryMessages())
                    .toolChoice(ToolChoice.NONE)
                    .build();

            ChatResponse chatResponse = chatModel.chat(chatRequest);

            if (executionContext.isInterrupted()) {
                throw new BusinessException(new InterruptedException("生成摘要被手动中断"));
            }

            AiMessage summeryMessage = chatResponse.aiMessage();
            String summery = summeryMessage.text();
            agentCallback.onPrompt(AgentPromptType.AGENT_MESSAGE_BLOCK, summery, false);

            List<ChatMessageDTO> summeryMessageDTOList = List.of(
                    ChatMessageDTO.builder()
                            .sessionId(executionContext.getSessionId())
                            .sessionCode(executionContext.getSessionCode())
                            .requestId(executionContext.getRequestId())
                            .role(MessageRole.AGENT_PROMPT.name())
                            .content("")
                            .chatContent(PersistableChatMessage.toJsonString(summeryUserMessage))
                            .metadata(new HashMap<>())
                            .build(),
                    ChatMessageDTO.builder()
                            .sessionId(executionContext.getSessionId())
                            .sessionCode(executionContext.getSessionCode())
                            .requestId(executionContext.getRequestId())
                            .role(MessageRole.ASSISTANT_PROMPT.name())
                            .content("")
                            .chatContent(PersistableChatMessage.toJsonString(summeryMessage))
                            .metadata(new HashMap<>())
                            .build(),
                    ChatMessageDTO.builder()
                            .sessionId(executionContext.getSessionId())
                            .sessionCode(executionContext.getSessionCode())
                            .requestId(executionContext.getRequestId())
                            .role(MessageRole.AGENT_MESSAGE.name())
                            .content(summery)
                            .metadata(new HashMap<>())
                            .build()
            );

            chatSessionService.archiveSession(executionContext.getSessionCode(), executionContext.getUserId(), summeryMessageDTOList);

            List<ChatMessage> newMessageList = new ArrayList<>(3);
            if (agentScope.get("SystemMessage") != null) {
                newMessageList.add((ChatMessage) agentScope.get("SystemMessage"));
            }
            newMessageList.add(summeryUserMessage);
            newMessageList.add(summeryMessage);
            chatMemoryChatMemoryStore.updateMessages(executionContext.getConnectSessionId(), newMessageList);
            if (agentScope.get("UserMessage") != null) {
                chatMemoryChatMemoryStore.addMessage(executionContext.getConnectSessionId(), (ChatMessage) agentScope.get("UserMessage"));
            }

            String tokenUsageRedisKey = RedisKeyConstants.getSessionTokenUsageKey(executionContext.getSessionCode());
            redisUtil.del(tokenUsageRedisKey);

            TokenUsage tokenUsage = chatResponse.tokenUsage();
            agentCallback.onTokenUsage(agentScope, tokenUsage.inputTokenCount(), tokenUsage.outputTokenCount(), tokenUsage.totalTokenCount(), chatMemoryChatMemoryStore.getMessages(executionContext.getConnectSessionId()).size());

            return chatResponse;
        }, agentExecutor);
    }

    private void completeOrder(AgentExecutionContext executionContext, boolean isSuccess, Throwable e) {
        AgentCallback agentCallback = executionContext.getAgentCallback();
        if (executionContext.getWebSocketSessionContext().completeExecution(executionContext.getRequestId())) {
            agentCallback.onPrompt(AgentPromptType.COMPLETE, null, false);
        }

        AgentExecutionLog executionLog = executionContext.getExecutionLog();
        if (executionLog != null) {
            if (!isSuccess) {
                if (executionContext.isInterrupted()) {
                    executionLog.setStatus(ExecutionStatus.INTERRUPTED);
                } else {
                    executionLog.setStatus(ExecutionStatus.FAILED);
                    executionLog.setErrorMessage(e != null ? e.getMessage() : "");
                }
            } else {
                executionLog.setStatus(ExecutionStatus.SUCCESS);
            }
            executionLog.setExecutionTimeMs(executionContext.getStartTime() != null ? System.currentTimeMillis() - executionContext.getStartTime() : null);
            executionLog.setOutput(TraceIdUtil.getTraceId());
            executionLogRepository.update(executionLog);
        }

        executionContext.executeCompleteCallbacks(isSuccess, e);
    }

}
