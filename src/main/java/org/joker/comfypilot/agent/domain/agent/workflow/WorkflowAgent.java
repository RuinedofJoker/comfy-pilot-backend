package org.joker.comfypilot.agent.domain.agent.workflow;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.event.*;
import org.joker.comfypilot.agent.domain.service.AbstractAgent;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.agent.domain.service.AgentConfigDefinition;
import org.joker.comfypilot.agent.infrastructure.tool.StatusUpdateTool;
import org.joker.comfypilot.agent.infrastructure.tool.TodoWriteTool;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerAdvancedFeaturesDTO;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerService;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.StreamingChatModelFactory;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.dto.client2server.UserMessageRequestData;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.enums.MessageStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.joker.comfypilot.agent.domain.react.ReactExecutor;
import dev.langchain4j.agent.tool.ToolSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 简单对话Agent
 * 用于基本的对话交互
 */
@Slf4j
@Component
public class WorkflowAgent extends AbstractAgent implements Agent {

    @Lazy
    @Autowired
    private ToolRegistry toolRegistry;
    @Autowired
    private StreamingChatModelFactory streamingChatModelFactory;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private ChatSessionService chatSessionService;
    @Autowired
    private ComfyuiServerService comfyuiServerService;
    @Autowired
    private ReactExecutor reactExecutor;

    @Override
    public String getAgentCode() {
        return "WORKFLOW_CHAT";
    }

    @Override
    public String getAgentName() {
        return "ComfyUI工作流编辑对话助手";
    }

    @Override
    public String getDescription() {
        return "提供ComfyUI工作流建议与指南的agent助手";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public List<AgentConfigDefinition> getConfigDefinitions() {
        return List.of(
                AgentConfigDefinition.modelValue("llmModelIdentifier", "使用的LLM模型", true, true, ModelCallingType.API_LLM),
                AgentConfigDefinition.stringValue("apiKey", "模型调用apiKey", false, true, ""),
                AgentConfigDefinition.floatValue("temperature", "模型调用温度", false, true, 0D, 1D),
                AgentConfigDefinition.intValue("maxTokens", "模型的最大Token数", false, true, 0, 200_000),
                AgentConfigDefinition.intValue("maxMessages", "上下文的最大消息数", true, false, 0, 1000)
        );
    }

    @Override
    public Map<String, Object> getAgentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.7);
        config.put("maxTokens", 200_000);
        config.put("maxMessages", 1000);
        return config;
    }

    @Override
    public Map<String, Object> getAgentScopeConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("SystemPrompt", WorkflowAgentPrompts.SYSTEM_PROMPT);
        return config;
    }

    protected void executeWithStreaming(AgentExecutionContext executionContext) throws Exception {
        AgentCallback agentCallback = executionContext.getAgentCallback();

        // 初始化事件发布器
        AgentEventPublisher eventPublisher = new AgentEventPublisher();
        executionContext.setEventPublisher(eventPublisher);

        // 注册消息添加后事件监听器（用于保存到数据库）
        eventPublisher.addEventListener(AgentEventType.AFTER_MESSAGE_ADD, (AfterMessageAddEvent event) -> {
            if (!event.isSuccess()) {
                log.error("消息添加到内存失败: sessionCode={}, messageType={}",
                    event.getContext().getSessionCode(), event.getMessageType());
                return;
            }

            try {
                // 保存 AI 消息到数据库
                org.joker.comfypilot.session.domain.entity.ChatMessage dbMessage =
                    org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                        .sessionId(event.getContext().getSessionId())
                        .sessionCode(event.getContext().getSessionCode())
                        .requestId(event.getContext().getRequestId())
                        .role(MessageRole.ASSISTANT)
                        .status(MessageStatus.ACTIVE)
                        .metadata(new HashMap<>())
                        .chatContent(PersistableChatMessage.toJsonString(event.getMessage()))
                        .build();
                chatMessageRepository.save(dbMessage);
                log.debug("消息已保存到数据库: sessionCode={}, messageType={}, iteration={}",
                    event.getContext().getSessionCode(), event.getMessageType(), event.getIteration());
            } catch (Exception e) {
                log.error("保存消息到数据库失败: sessionCode={}", event.getContext().getSessionCode(), e);
            }
        });

        AgentExecutionRequest request = executionContext.getRequest();
        String userMessage = request.getUserMessage();
        UserMessageRequestData userMessageData = request.getUserMessageData();
        Map<String, Object> agentConfig = getRuntimeAgentConfig(executionContext);
        Map<String, Object> agentScope = executionContext.getAgentScope();

        // 从 ToolRegistry 获取工具列表
        List<Tool> todoTools = toolRegistry.getToolsByClass(TodoWriteTool.class);
        List<Tool> statusTools = toolRegistry.getToolsByClass(StatusUpdateTool.class);

        // 创建流式聊天模型（工具规范将在调用时通过 ChatRequest 传递）
        StreamingChatModel streamingModel = streamingChatModelFactory.createStreamingChatModel(
                (String) agentConfig.get("llmModelIdentifier"),
                agentConfig
        );

        if (userMessage.startsWith("/")) {
            // TODO 命令执行
        }

        // 构建用户消息+Agent提示词
        StringBuilder userMessageBuilder = new StringBuilder();
        userMessageBuilder.append(WorkflowAgentPrompts.USER_QUERY_START_TOKEN).append(userMessage).append(WorkflowAgentPrompts.USER_QUERY_END_TOKEN).append("\n");
        String workflowContent = userMessageData.getWorkflowContent();
        userMessageBuilder.append(WorkflowAgentPrompts.USER_WORKFLOW_PROMPT.formatted(workflowContent)).append("\n");

        // Agent构建ComfyUI服务高级功能提示词和补充工具
        ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(executionContext.getSessionCode());
        ComfyuiServerDTO comfyuiServerDTO = comfyuiServerService.getById(chatSessionDTO.getComfyuiServerId());
        if (Boolean.TRUE.equals(comfyuiServerDTO.getAdvancedFeaturesEnabled()) && comfyuiServerDTO.getAdvancedFeatures() != null) {
            ComfyuiServerAdvancedFeaturesDTO advancedFeatures = comfyuiServerDTO.getAdvancedFeatures();

            //
        }

        // 添加系统提示词
        agentCallback.addMemoryMessage(SystemMessage.from(agentScope.get("SystemPrompt").toString()), null, null);

        // 添加用户提示词
        agentCallback.addMemoryMessage(UserMessage.from(
                List.of(TextContent.from(userMessageBuilder.toString()))
        ), (chatMessage) -> {
            // 保存用户消息
            org.joker.comfypilot.session.domain.entity.ChatMessage systemChatMessage = org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                    .sessionId(executionContext.getSessionId())
                    .sessionCode(executionContext.getSessionCode())
                    .requestId(executionContext.getRequestId())
                    .role(userMessage.startsWith("/") ? MessageRole.USER_ORDER : MessageRole.USER)
                    .status(MessageStatus.ACTIVE)
                    .metadata(new HashMap<>())
                    .content(userMessage)
                    .chatContent(PersistableChatMessage.toJsonString(chatMessage))
                    .build();
            chatMessageRepository.save(systemChatMessage);
        }, null);

        // 准备工具规范
        List<ToolSpecification> toolSpecs = new ArrayList<>();

        // 添加内置工具
        toolSpecs.addAll(todoTools.stream().map(Tool::toolSpecification).toList());
        toolSpecs.addAll(statusTools.stream().map(Tool::toolSpecification).toList());

        // 添加用户提供的 MCP 工具
        if (userMessageData.getToolSchemas() != null && !userMessageData.getToolSchemas().isEmpty()) {
            toolSpecs.addAll(executionContext.getClientTools().stream().map(Tool::toolSpecification).toList());
        }

        // 构建 ChatRequest
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(agentCallback.getMemoryMessages())
                .toolSpecifications(toolSpecs)
                .toolChoice(ToolChoice.AUTO)
                .build();

        // 执行 ReAct 循环（响应式，非阻塞）
        reactExecutor.executeReactLoop(streamingModel, chatRequest, executionContext);

        // 注意：不再需要手动调用 onStreamComplete，ReactExecutor 会在完成时自动调用
    }

    private Map<String, Object> getRuntimeAgentConfig(AgentExecutionContext executionContext) {
        Map<String, Object> defaultAgentConfig = new HashMap<>(getAgentConfig());
        Map<String, Object> sessionAgentConfig = new HashMap<>(executionContext.getAgentConfig());

        for (AgentConfigDefinition configDefinition : getConfigDefinitions()) {
            if (configDefinition.userOverride() && sessionAgentConfig.get(configDefinition.name()) != null) {
                defaultAgentConfig.put(configDefinition.name(), sessionAgentConfig.get(configDefinition.name()));
            }
        }

        return defaultAgentConfig;
    }
}
