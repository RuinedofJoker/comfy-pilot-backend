package org.joker.comfypilot.agent.domain.agent.workflow;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.output.TokenUsage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.entity.AgentExecutionLog;
import org.joker.comfypilot.agent.domain.enums.ExecutionStatus;
import org.joker.comfypilot.agent.domain.event.*;
import org.joker.comfypilot.agent.domain.repository.AgentExecutionLogRepository;
import org.joker.comfypilot.agent.domain.service.AbstractAgent;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.agent.domain.service.AgentConfigDefinition;
import org.joker.comfypilot.agent.domain.toolcall.ToolCallWaitManager;
import org.joker.comfypilot.agent.infrastructure.tool.StatusUpdateTool;
import org.joker.comfypilot.agent.infrastructure.tool.TodoWriteTool;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiDirectoryConfigDTO;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerAdvancedFeaturesDTO;
import org.joker.comfypilot.cfsvr.application.dto.ComfyuiServerDTO;
import org.joker.comfypilot.cfsvr.application.service.ComfyuiServerService;
import org.joker.comfypilot.cfsvr.application.tool.ComfyUIServerTool;
import org.joker.comfypilot.cfsvr.domain.enums.ConnectionType;
import org.joker.comfypilot.common.config.JacksonConfig;
import org.joker.comfypilot.common.domain.content.*;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.common.event.WorkflowAgentOnPromptEvent;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.common.tool.command.ComfyUILocalCommandTools;
import org.joker.comfypilot.common.tool.filesystem.ServerFileSystemTools;
import org.joker.comfypilot.common.util.TraceIdUtil;
import org.joker.comfypilot.model.application.dto.AiModelDTO;
import org.joker.comfypilot.model.application.service.AiModelService;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.StreamingChatModelFactory;
import org.joker.comfypilot.script.context.ScriptRuntimeContext;
import org.joker.comfypilot.script.tool.PythonScriptTools;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.dto.client2server.UserMessageRequestData;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.session.domain.enums.MessageStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.session.infrastructure.websocket.WebSocketSessionManager;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
import org.joker.comfypilot.agent.domain.react.ReactExecutor;
import dev.langchain4j.agent.tool.ToolSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    @Autowired
    private WebSocketSessionManager sessionManager;
    @Autowired
    private ToolCallWaitManager toolCallWaitManager;
    @Autowired
    private AgentExecutionLogRepository executionLogRepository;
    @Autowired
    @Qualifier("workflowAgentOnPromptEventBus")
    private EventBus workflowAgentOnPromptEventBus;

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
                AgentConfigDefinition.modelValue("llmModelIdentifier", "使用的LLM模型", true, true, ModelCallingType.API_LLM)
        );
    }

    @Override
    public Map<String, Object> getAgentConfig() {
        return new LinkedHashMap<>();
    }

    @Override
    public Map<String, Object> getAgentScopeConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("SystemPrompt", WorkflowAgentPrompts.SYSTEM_PROMPT);
        return config;
    }

    protected void executeWithStreaming(AgentExecutionContext executionContext) throws Exception {
        try {
            executeWithAgentStreaming(executionContext);
        } catch (Exception e) {
            executionContext.getAgentCallback().onPrompt(AgentPromptType.ERROR, "执行过程中发生错误，" + e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        workflowAgentOnPromptEventBus.register(this);
    }

    @Subscribe
    private void onPrompt(WorkflowAgentOnPromptEvent event) {
        AgentExecutionContext executionContext = event.getExecutionContext();
        if (executionContext == null) {
            throw new BusinessException("Agent执行上下文为空");
        }
        if (AgentPromptType.TODO_WRITE.equals(event.getPromptType())) {
            // 保存用户消息
            org.joker.comfypilot.session.domain.entity.ChatMessage agentPlanChatMessage = org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                    .sessionId(executionContext.getSessionId())
                    .sessionCode(executionContext.getSessionCode())
                    .requestId(executionContext.getRequestId())
                    .role(MessageRole.AGENT_PLAN)
                    .status(MessageStatus.ACTIVE)
                    .metadata(new HashMap<>())
                    .content(event.getMessage())
                    .chatContent(null)
                    .build();
            chatMessageRepository.save(agentPlanChatMessage);
            executionContext.getAgentCallback().onPrompt(AgentPromptType.TODO_WRITE, event.getMessage());
        } else {
            throw new BusinessException("不能识别的消息类型");
        }
    }

    private void executeWithAgentStreaming(AgentExecutionContext executionContext) throws Exception {
        AgentCallback agentCallback = executionContext.getAgentCallback();

        AgentExecutionRequest request = executionContext.getRequest();
        String userMessage = request.getUserMessage();
        UserMessageRequestData userMessageData = request.getUserMessageData();
        Map<String, Object> agentConfig = getRuntimeAgentConfig(executionContext);
        Map<String, Object> agentScope = executionContext.getAgentScope();

        AiModelDTO llmModel = aiModelService.getByModelIdentifier((String) agentConfig.get("llmModelIdentifier"));
        ObjectMapper objectMapper = JacksonConfig.getObjectMapper();
        Map<String, Object> modelConfig = objectMapper.readValue(llmModel.getModelConfig(), new TypeReference<>() {
        });

        agentScope.put("connectSessionId", executionContext.getConnectSessionId());
        agentScope.put("userId", executionContext.getUserId());
        agentScope.put("sessionCode", executionContext.getSessionCode());
        agentScope.put("llmModelConfig", modelConfig);

        Integer maxTokens = modelConfig.get("maxTokens") != null ? Integer.parseInt(modelConfig.get("maxTokens").toString()) : 200_000;
        Integer maxMessages = modelConfig.get("maxMessages") != null ? Integer.parseInt(modelConfig.get("maxMessages").toString()) : 500;

        agentScope.put("maxTokens", maxTokens);
        agentScope.put("maxMessages", maxMessages);

        // 创建流式聊天模型（工具规范将在调用时通过 ChatRequest 传递）
        StreamingChatModel streamingModel = streamingChatModelFactory.createStreamingChatModel(
                (String) agentConfig.get("llmModelIdentifier"),
                agentConfig
        );

        if (userMessage.startsWith("/")) {
            // TODO 命令执行
        } else {
            // Agent执行

            ConcurrentLinkedQueue<String> messageIds = new ConcurrentLinkedQueue<>();

            // 构建用户消息+Agent提示词
            StringBuilder userMessageBuilder = new StringBuilder();
            agentScope.put("UserMessage", userMessage);
            userMessageBuilder.append(WorkflowAgentPrompts.USER_QUERY_START_TOKEN).append(userMessage).append(WorkflowAgentPrompts.USER_QUERY_END_TOKEN);
            List<ChatContent> multimodalContents = userMessageData.getMultimodalContents();

            // 从 ToolRegistry 获取工具列表
            List<Tool> todoTools = toolRegistry.getToolsByClass(TodoWriteTool.class);
            List<Tool> statusTools = toolRegistry.getToolsByClass(StatusUpdateTool.class);
            List<Tool> comfyuiServerTools = toolRegistry.getToolsByClass(ComfyUIServerTool.class);
            List<Tool> serverFileSystemTools = toolRegistry.getToolsByClass(ServerFileSystemTools.class);
            List<Tool> pythonScriptTools = toolRegistry.getToolsByClass(PythonScriptTools.class);

            // 准备工具规范
            List<ToolSpecification> toolSpecs = new ArrayList<>();

            // 添加客户端的工具
            if (userMessageData.getToolSchemas() != null && !userMessageData.getToolSchemas().isEmpty()) {
                toolSpecs.addAll(executionContext.getClientTools().stream().map(Tool::toolSpecification).toList());
            }

            // 添加内置工具
            for (Tool serverTool : todoTools) {
                if (executionContext.getClientToolNames().contains(serverTool.toolName())) {
                    throw new BusinessException("客户端工具" + serverTool.toolName() + "与服务内部工具重名");
                }
            }
            toolSpecs.addAll(todoTools.stream().map(Tool::toolSpecification).toList());
            for (Tool serverTool : statusTools) {
                if (executionContext.getClientToolNames().contains(serverTool.toolName())) {
                    throw new BusinessException("客户端工具" + serverTool.toolName() + "与服务内部工具重名");
                }
            }
            toolSpecs.addAll(statusTools.stream().map(Tool::toolSpecification).toList());
            for (Tool comfyuiServerTool : comfyuiServerTools) {
                if (executionContext.getClientToolNames().contains(comfyuiServerTool.toolName())) {
                    throw new BusinessException("客户端工具" + comfyuiServerTool.toolName() + "与服务内部工具重名");
                }
            }
            toolSpecs.addAll(comfyuiServerTools.stream().map(Tool::toolSpecification).toList());

            if (ScriptRuntimeContext.isPythonAvailable()) {
                // 添加python脚本的系统提示词
                StringBuilder systemMessageBuilder = new StringBuilder(agentScope.get("SystemPrompt").toString()).append("\n");

                systemMessageBuilder.append(WorkflowAgentPrompts.SERVER_FILE_TOOL_PROMPT).append("\n");
                systemMessageBuilder.append(WorkflowAgentPrompts.SERVER_PYTHON_TOOL_PROMPT).append("\n");

                for (Tool serverFileSystemTool : serverFileSystemTools) {
                    if (executionContext.getClientToolNames().contains(serverFileSystemTool.toolName())) {
                        throw new BusinessException("客户端工具" + serverFileSystemTool.toolName() + "与服务内部工具重名");
                    }
                }
                toolSpecs.addAll(serverFileSystemTools.stream().map(Tool::toolSpecification).toList());

                for (Tool pythonScriptTool : pythonScriptTools) {
                    if (executionContext.getClientToolNames().contains(pythonScriptTool.toolName())) {
                        throw new BusinessException("客户端工具" + pythonScriptTool.toolName() + "与服务内部工具重名");
                    }
                }
                toolSpecs.addAll(pythonScriptTools.stream().map(Tool::toolSpecification).toList());

                agentScope.put("SystemPrompt", systemMessageBuilder.toString());
            }

            // Agent构建ComfyUI服务高级功能提示词和补充工具
            ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(executionContext.getSessionCode());
            ComfyuiServerDTO comfyuiServerDTO = comfyuiServerService.getById(chatSessionDTO.getComfyuiServerId());
            if (Boolean.TRUE.equals(comfyuiServerDTO.getAdvancedFeaturesEnabled()) && comfyuiServerDTO.getAdvancedFeatures() != null) {
                ComfyuiServerAdvancedFeaturesDTO advancedFeatures = comfyuiServerDTO.getAdvancedFeatures();

                agentScope.put("advancedFeaturesEnabled", true);
                agentScope.put("advancedFeatures", advancedFeatures);
                ComfyuiDirectoryConfigDTO directoryConfig = advancedFeatures.getDirectoryConfig();

                // 添加高级功能的系统提示词
                StringBuilder systemMessageBuilder = new StringBuilder(agentScope.get("SystemPrompt").toString()).append("\n");

                if (ConnectionType.LOCAL.getCode().equals(advancedFeatures.getConnectionType())) {
                    List<Tool> comfyUILocalCommandTools = toolRegistry.getToolsByClass(ComfyUILocalCommandTools.class);
                    for (Tool comfyUILocalCommandTool : comfyUILocalCommandTools) {
                        if (executionContext.getClientToolNames().contains(comfyUILocalCommandTool.toolName())) {
                            throw new BusinessException("客户端工具" + comfyUILocalCommandTool.toolName() + "与服务内部工具重名");
                        }
                    }
                    toolSpecs.addAll(comfyUILocalCommandTools.stream().map(Tool::toolSpecification).toList());

                    systemMessageBuilder.append(WorkflowAgentPrompts.COMFY_UI_LOCAL_ADVANCED_PROMPT
                            .formatted(
                                    advancedFeatures.getOsType(),
                                    advancedFeatures.getWorkingDirectory(),
                                    advancedFeatures.getPythonCommand(),
                                    directoryConfig != null ? directoryConfig.getComfyuiInstallPath() : null,
                                    directoryConfig != null ? directoryConfig.getComfyuiStartupPath() : null
                            )).append("\n");
                } else {

                }

                agentScope.put("SystemPrompt", systemMessageBuilder.toString());
            }

            List<Content> userMessageContent = new ArrayList<>(1 + (CollectionUtils.isNotEmpty(multimodalContents) ? multimodalContents.size() : 0));
            List<Content> userMultimodalContents = new ArrayList<>(CollectionUtils.isNotEmpty(multimodalContents) ? multimodalContents.size() : 0);

            if (CollectionUtils.isNotEmpty(multimodalContents)) {


                int imageIndex = 0;
                int videoIndex = 0;
                int audioIndex = 0;
                int pdfFileIndex = 0;
                Map<String, ImageChatContent> imageContentMap = new LinkedHashMap<>();
                Map<String, VideoChatContent> videoContentMap = new HashMap<>();
                Map<String, AudioChatContent> audioContentMap = new HashMap<>();
                Map<String, PdfChatContent> pdfFileContentMap = new HashMap<>();
                for (ChatContent multimodalContent : multimodalContents) {
                    if (multimodalContent instanceof ImageChatContent imageChatContent) {
                        imageContentMap.put("image_" + imageIndex++, imageChatContent);
                        if (Boolean.TRUE.equals(modelConfig.get("supportImageMultimodal"))) {
                            userMultimodalContents.add(multimodalContent.toContent());
                        }
                    } else if (multimodalContent instanceof VideoChatContent videoChatContent) {
                        videoContentMap.put("video_" + videoIndex++, videoChatContent);
                        if (Boolean.TRUE.equals(modelConfig.get("supportVideoMultimodal"))) {
                            userMultimodalContents.add(multimodalContent.toContent());
                        }
                    } else if (multimodalContent instanceof AudioChatContent audioChatContent) {
                        audioContentMap.put("audio_" + audioIndex++, audioChatContent);
                        if (Boolean.TRUE.equals(modelConfig.get("supportAudioMultimodal"))) {
                            userMultimodalContents.add(multimodalContent.toContent());
                        }
                    } else if (multimodalContent instanceof PdfChatContent pdfFileChatContent) {
                        pdfFileContentMap.put("pdfFile_" + pdfFileIndex++, pdfFileChatContent);
                        if (Boolean.TRUE.equals(modelConfig.get("supportPdfFileMultimodal"))) {
                            userMultimodalContents.add(multimodalContent.toContent());
                        }
                    }
                }

                userMessageBuilder.append("\n").append(WorkflowAgentPrompts.USER_MULTIMODAL_CONTENT_PROMPT.formatted(
                        !imageContentMap.isEmpty() ? "`image`" : "",
                        !videoContentMap.isEmpty() ? "`video`" : "",
                        !audioContentMap.isEmpty() ? "`audio`" : "",
                        !pdfFileContentMap.isEmpty() ? "`pdfFile`" : "",
                        !imageContentMap.isEmpty() ? "image: " + JSON.toJSONString(imageContentMap.keySet()) : "",
                        !videoContentMap.isEmpty() ? "video: " + JSON.toJSONString(videoContentMap.keySet()) : "",
                        !audioContentMap.isEmpty() ? "audio: " + JSON.toJSONString(audioContentMap.keySet()) : "",
                        !pdfFileContentMap.isEmpty() ? "pdfFile: " + JSON.toJSONString(pdfFileContentMap.keySet()) : ""
                ));
                agentScope.put("UserImageContents", imageContentMap);
                agentScope.put("UserVideoContents", videoContentMap);
                agentScope.put("UserAudioContents", audioContentMap);
                agentScope.put("UserPdfFileContents", pdfFileContentMap);
            }

            if (agentScope.get("UserRules") != null) {
                userMessageBuilder.append("\n").append(WorkflowAgentPrompts.USER_RULES_PROMPT.formatted(agentScope.get("UserRules")));
            }

            // 添加系统提示词
            agentCallback.addMemoryMessage(SystemMessage.from(agentScope.get("SystemPrompt").toString()), null, null);

            // 添加用户提示词
            userMessageContent.add(TextContent.from(userMessageBuilder.toString()));
            if (CollectionUtils.isNotEmpty(userMultimodalContents)) {
                userMessageContent.addAll(userMultimodalContents);
            }
            agentCallback.addMemoryMessage(UserMessage.from(
                    userMessageContent
            ), (chatMessage) -> {
                // 保存用户消息
                org.joker.comfypilot.session.domain.entity.ChatMessage userChatMessage = org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                        .sessionId(executionContext.getSessionId())
                        .sessionCode(executionContext.getSessionCode())
                        .requestId(executionContext.getRequestId())
                        .role(userMessage.startsWith("/") ? MessageRole.USER_ORDER : MessageRole.USER)
                        .status(MessageStatus.ACTIVE)
                        .metadata(new HashMap<>())
                        .content(userMessage)
                        .chatContent(PersistableChatMessage.toJsonString(chatMessage))
                        .build();
                userChatMessage = chatMessageRepository.save(userChatMessage);
                messageIds.add(userChatMessage.getId() + "");
            }, null);

            // 构建 ChatRequest
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(agentCallback.getMemoryMessages())
                    .toolSpecifications(toolSpecs)
                    .toolChoice(ToolChoice.REQUIRED)
                    .build();

            AtomicReference<StringBuilder> streamOutputBuilder = new AtomicReference<>();

            // 初始化事件发布器
            AgentEventPublisher eventPublisher = new AgentEventPublisher();
            executionContext.setEventPublisher(eventPublisher);

            // ==================== 注册事件监听器 ====================
            // LLM调用前事件
            eventPublisher.addEventListener(AgentEventType.BEFORE_LLM_CALL, (BeforeLlmCallEvent event) -> {
                streamOutputBuilder.compareAndSet(null, new StringBuilder());
                agentCallback.onPrompt(AgentPromptType.THINKING, null);
            });

            eventPublisher.addEventListener(AgentEventType.BEFORE_TOOL_CALL, (BeforeToolCallEvent event) -> {
                agentCallback.onPrompt(AgentPromptType.TOOL_CALLING, null);
            });

            eventPublisher.addEventListener(AgentEventType.AFTER_TOOL_CALL, (AfterToolCallEvent event) -> {
                agentCallback.onPrompt(AgentPromptType.TOOL_COMPLETE, null);
            });

            // 流式输出事件 -> AgentCallback.onStream()
            eventPublisher.addEventListener(AgentEventType.STREAM, (StreamEvent event) -> {
                agentCallback.onStream(event.getChunk());
                if (streamOutputBuilder.get() != null) {
                    streamOutputBuilder.get().append(event.getChunk());
                }
            });

            // 流式输出完成事件 -> AgentCallback.onStreamComplete()
            eventPublisher.addEventListener(AgentEventType.STREAM_COMPLETE, (StreamCompleteEvent event) -> {
                streamOutputBuilder.compareAndSet(streamOutputBuilder.get(), null);
                TokenUsage tokenUsage = event.getCompleteResponse().tokenUsage();
                agentCallback.onStreamComplete(agentScope, event.getCompleteResponse().aiMessage().text(), tokenUsage.inputTokenCount(), tokenUsage.outputTokenCount(), tokenUsage.totalTokenCount(), agentCallback.getMemoryMessages().size());
            });

            AtomicBoolean isStarted = new AtomicBoolean(false);
            // 迭代开始事件
            eventPublisher.addEventListener(AgentEventType.ITERATION_START, (IterationStartEvent event) -> {
                if (isStarted.compareAndSet(false, true)) {
                    agentCallback.onPrompt(AgentPromptType.STARTED, null);
                }
            });

            // 迭代结束事件
            eventPublisher.addEventListener(AgentEventType.ITERATION_END, (IterationEndEvent event) -> {
                // 发生中断
                if (!event.isSuccess() && !event.isWillContinue() && event.getContext().isInterrupted() && streamOutputBuilder.get() != null) {
                    StringBuilder streamOutput = streamOutputBuilder.get();
                    if (streamOutputBuilder.compareAndSet(streamOutput, null)) {
                        // 保存输出到一半的消息
                        if (streamOutput != null && !streamOutput.isEmpty()) {
                            org.joker.comfypilot.session.domain.entity.ChatMessage dbMessage =
                                    org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                                            .sessionId(event.getContext().getSessionId())
                                            .sessionCode(event.getContext().getSessionCode())
                                            .requestId(event.getContext().getRequestId())
                                            .role(MessageRole.ASSISTANT)
                                            .status(MessageStatus.ACTIVE)
                                            .metadata(new HashMap<>())
                                            .content("")
                                            .chatContent(PersistableChatMessage.toJsonString(AiMessage.aiMessage(streamOutput.toString())))
                                            .build();
                            chatMessageRepository.save(dbMessage);
                        }
                    }
                    agentCallback.onInterrupted();
                } else if (!event.isSuccess() && !event.isWillContinue()) {
                    agentCallback.onPrompt(AgentPromptType.ERROR, event.getException() != null ? event.getException().getMessage() : "未知错误");
                }

                if (!event.isWillContinue() && executionContext.getExecutionLog() != null) {
                    // 记录日志
                    AgentExecutionLog executionLog = executionContext.getExecutionLog();
                    executionLog.setStatus(event.getContext().isInterrupted() ? ExecutionStatus.INTERRUPTED : (event.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILED));
                    if (event.getException() != null) {
                        log.error("Agent执行失败", event.getException());
                        executionLog.setErrorMessage("traceId: " + TraceIdUtil.getTraceId() + " ; " + event.getException().getMessage());
                    }
                    executionLog.setExecutionTimeMs(executionContext.getStartTime() != null ? System.currentTimeMillis() - executionContext.getStartTime() : null);
                    executionLog.setOutput(messageIds.toString());
                    executionLogRepository.update(executionLog);
                }

                if (!event.isWillContinue()) {
                    if (executionContext.getWebSocketSessionContext().completeExecution(executionContext.getRequestId())) {
                        agentCallback.onPrompt(AgentPromptType.COMPLETE, null);
                    }
                }
            });

            // 工具调用通知事件 -> AgentCallback.onToolCall()
            eventPublisher.addEventListener(AgentEventType.TOOL_CALL_NOTIFY, (ToolCallNotifyEvent event) -> {
                agentCallback.onToolCall(executionContext.getClientToolNames().contains(event.getToolName()), false, event.getToolCallId(), event.getToolName(), event.getToolArgs());
                // 连接断开时直接取消回调
                sessionManager.addRemovedCallback(executionContext.getConnectSessionId(), () -> {
                    toolCallWaitManager.cancelWait(event.getToolCallId(), event.getToolName());
                });
            });

            // 消息添加后事件 -> 保存到数据库
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
                    dbMessage = chatMessageRepository.save(dbMessage);
                    log.debug("消息已保存到数据库: sessionCode={}, messageType={}, iteration={}",
                            event.getContext().getSessionCode(), event.getMessageType(), event.getIteration());
                    if (messageRole.equals(MessageRole.ASSISTANT)) {
                        messageIds.add(dbMessage.getId() + "");
                    }
                } catch (Exception e) {
                    log.error("保存消息到数据库失败: sessionCode={}", event.getContext().getSessionCode(), e);
                    throw new BusinessException("保存消息到数据库失败");
                }
            });

            // 执行 ReAct 循环（响应式，非阻塞）
            reactExecutor.executeReactLoop(streamingModel, chatRequest, executionContext, 100);
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
