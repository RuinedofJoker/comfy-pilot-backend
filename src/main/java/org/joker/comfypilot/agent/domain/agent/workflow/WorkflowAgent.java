package org.joker.comfypilot.agent.domain.agent.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import org.joker.comfypilot.common.domain.content.*;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.service.AiModelService;
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
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 简单对话Agent
 * 用于基本的对话交互
 */
@Slf4j
@Component
public class WorkflowAgent extends AbstractAgent implements Agent {

    @Autowired
    private ToolRegistry toolRegistry;
    @Autowired
    private StreamingChatModelFactory streamingChatModelFactory;
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private AiModelService aiModelService;
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

        AgentExecutionRequest request = executionContext.getRequest();
        String userMessage = request.getUserMessage();
        UserMessageRequestData userMessageData = request.getUserMessageData();
        Map<String, Object> agentConfig = getRuntimeAgentConfig(executionContext);
        Map<String, Object> agentScope = executionContext.getAgentScope();

        AiModelDTO llmModel = aiModelService.getByModelIdentifier((String) agentConfig.get("llmModelIdentifier"));
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> modelConfig = objectMapper.readValue(llmModel.getModelConfig(), new TypeReference<>() {
        });

        // 创建流式聊天模型（工具规范将在调用时通过 ChatRequest 传递）
        StreamingChatModel streamingModel = streamingChatModelFactory.createStreamingChatModel(
                (String) agentConfig.get("llmModelIdentifier"),
                agentConfig
        );

        if (userMessage.startsWith("/")) {
            // TODO 命令执行
        } else {
            // Agent执行

            // 构建用户消息+Agent提示词
            StringBuilder userMessageBuilder = new StringBuilder();
            userMessageBuilder.append(WorkflowAgentPrompts.USER_QUERY_START_TOKEN).append(userMessage).append(WorkflowAgentPrompts.USER_QUERY_END_TOKEN).append("\n");
            String workflowContent = userMessageData.getWorkflowContent();
            List<ChatContent> multimodalContents = userMessageData.getMultimodalContents();
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
            List<Content> userMessageContent = new ArrayList<>(1 + (CollectionUtils.isNotEmpty(multimodalContents) ? multimodalContents.size() : 0));
            userMessageContent.add(TextContent.from(userMessageBuilder.toString()));
            if (CollectionUtils.isNotEmpty(multimodalContents)) {
                for (ChatContent multimodalContent : multimodalContents) {
                    if ((multimodalContent instanceof ImageChatContent) && !Boolean.TRUE.equals(modelConfig.get("supportImageMultimodal"))) {
                        throw new BusinessException("模型不支持图片多模态文件");
                    } else if ((multimodalContent instanceof VideoChatContent) && !Boolean.TRUE.equals(modelConfig.get("supportVideoMultimodal"))) {
                        throw new BusinessException("模型不支持图片多模态文件");
                    } else if ((multimodalContent instanceof AudioChatContent) && !Boolean.TRUE.equals(modelConfig.get("supportAudioMultimodal"))) {
                        throw new BusinessException("模型不支持图片多模态文件");
                    } else if ((multimodalContent instanceof PdfChatContent) && !Boolean.TRUE.equals(modelConfig.get("supportPdfFileMultimodal"))) {
                        throw new BusinessException("模型不支持图片多模态文件");
                    }
                    userMessageContent.add(multimodalContent.toContent());
                }
            }
            agentCallback.addMemoryMessage(UserMessage.from(
                    userMessageContent
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

            // 从 ToolRegistry 获取工具列表
            List<Tool> todoTools = toolRegistry.getToolsByClass(TodoWriteTool.class);
            List<Tool> statusTools = toolRegistry.getToolsByClass(StatusUpdateTool.class);

            // 准备工具规范
            List<ToolSpecification> toolSpecs = new ArrayList<>();

            // 添加用户提供的 MCP 工具
            if (userMessageData.getToolSchemas() != null && !userMessageData.getToolSchemas().isEmpty()) {
                toolSpecs.addAll(executionContext.getClientTools().stream().map(Tool::toolSpecification).toList());
            }

            // 添加内置工具
            for (Tool serverTool : todoTools) {
                if (executionContext.getClientToolNames().contains(serverTool.toolName())) {
                    throw new BusinessException("客户端工具" + serverTool.toolName() + "与服务内部工具重名");
                }
            }
            for (Tool serverTool : statusTools) {
                if (executionContext.getClientToolNames().contains(serverTool.toolName())) {
                    throw new BusinessException("客户端工具" + serverTool.toolName() + "与服务内部工具重名");
                }
            }
            toolSpecs.addAll(todoTools.stream().map(Tool::toolSpecification).toList());
            toolSpecs.addAll(statusTools.stream().map(Tool::toolSpecification).toList());

            // 构建 ChatRequest
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(agentCallback.getMemoryMessages())
                    .toolSpecifications(toolSpecs)
                    .toolChoice(ToolChoice.AUTO)
                    .build();

            // 初始化事件发布器
            AgentEventPublisher eventPublisher = new AgentEventPublisher();
            executionContext.setEventPublisher(eventPublisher);

            // ==================== 注册事件监听器 ====================

            // 1. 流式输出事件 -> AgentCallback.onStream()
            eventPublisher.addEventListener(AgentEventType.STREAM, (StreamEvent event) -> {
                agentCallback.onStream(event.getChunk());
            });

            // 2. 流式输出完成事件 -> AgentCallback.onStreamComplete()
            eventPublisher.addEventListener(AgentEventType.STREAM_COMPLETE, (StreamCompleteEvent event) -> {
                if (event.isSuccess()) {
                    agentCallback.onStreamComplete(event.getFullContent());
                } else {
                    log.error("流式输出失败: sessionCode={}, error={}",
                            event.getContext().getSessionCode(), event.getErrorMessage());
                    agentCallback.onStreamComplete(null);
                }
            });

            // 3. 提示消息事件 -> AgentCallback.onPrompt()
            eventPublisher.addEventListener(AgentEventType.PROMPT, (PromptEvent event) -> {
                agentCallback.onPrompt(event.getPromptType(), event.getMessage());
            });

            // 4. 工具调用通知事件 -> AgentCallback.onToolCall()
            eventPublisher.addEventListener(AgentEventType.TOOL_CALL_NOTIFY, (ToolCallNotifyEvent event) -> {
                agentCallback.onToolCall(executionContext.getClientToolNames().contains(event.getToolName()), event.getToolCallId(), event.getToolName(), event.getToolArgs());
            });

            // 5. 消息添加后事件 -> 保存到数据库
            eventPublisher.addEventListener(AgentEventType.AFTER_MESSAGE_ADD, (AfterMessageAddEvent event) -> {
                if (!event.isSuccess()) {
                    log.error("消息添加到内存失败: sessionCode={}, messageType={}",
                            event.getContext().getSessionCode(), event.getMessageType());
                    return;
                }

                try {
                    ChatMessage langchainChatMessage = event.getMessage();
                    // 保存消息到数据库（仅保存 AI/Tool 消息，User 消息已在其他地方保存）
                    MessageRole messageRole = switch (langchainChatMessage) {
                        case AiMessage aiMessage -> MessageRole.ASSISTANT;
                        case UserMessage message -> MessageRole.AGENT_PROMPT;
                        case ToolExecutionResultMessage toolExecutionResultMessage -> MessageRole.TOOL_EXECUTION_RESULT;
                        default -> throw new BusinessException("未知的消息类型：" + langchainChatMessage.getClass());
                    };
                    org.joker.comfypilot.session.domain.entity.ChatMessage dbMessage =
                            org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                                    .sessionId(event.getContext().getSessionId())
                                    .sessionCode(event.getContext().getSessionCode())
                                    .requestId(event.getContext().getRequestId())
                                    .role(messageRole)
                                    .status(MessageStatus.ACTIVE)
                                    .metadata(new HashMap<>())
                                    .content("")
                                    .chatContent(PersistableChatMessage.toJsonString(langchainChatMessage))
                                    .build();
                    chatMessageRepository.save(dbMessage);
                    log.debug("消息已保存到数据库: sessionCode={}, messageType={}, iteration={}",
                            event.getContext().getSessionCode(), event.getMessageType(), event.getIteration());
                } catch (Exception e) {
                    log.error("保存消息到数据库失败: sessionCode={}", event.getContext().getSessionCode(), e);
                }
            });

            // 执行 ReAct 循环（响应式，非阻塞）
            reactExecutor.executeReactLoop(streamingModel, chatRequest, executionContext, 10);
        }
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
