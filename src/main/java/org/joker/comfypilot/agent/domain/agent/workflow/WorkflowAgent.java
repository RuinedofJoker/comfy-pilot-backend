package org.joker.comfypilot.agent.domain.agent.workflow;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.domain.callback.AgentCallback;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.service.AbstractAgent;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.agent.domain.service.AgentConfigDefinition;
import org.joker.comfypilot.agent.infrastructure.tool.StatusUpdateTool;
import org.joker.comfypilot.agent.infrastructure.tool.TodoWriteTool;
import org.joker.comfypilot.common.domain.message.PersistableChatMessage;
import org.joker.comfypilot.common.enums.MessageRole;
import org.joker.comfypilot.model.domain.enums.ModelCallingType;
import org.joker.comfypilot.model.domain.service.StreamingChatModelFactory;
import org.joker.comfypilot.session.domain.enums.MessageStatus;
import org.joker.comfypilot.session.domain.repository.ChatMessageRepository;
import org.joker.comfypilot.tool.domain.service.Tool;
import org.joker.comfypilot.session.domain.enums.AgentPromptType;
import org.joker.comfypilot.tool.domain.service.ToolRegistry;
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
        config.put("SystemPrompt", "你是一个友好的AI助手，专注于回答用户的问题。请保持回答简洁、准确、有帮助。");
        return config;
    }

    protected void executeWithStreaming(AgentExecutionContext executionContext) throws Exception {
        AgentCallback agentCallback = executionContext.getAgentCallback();
        // 发送ai思考中消息
        agentCallback.onPrompt(AgentPromptType.THINKING, null);

        AgentExecutionRequest request = executionContext.getRequest();
        String userMessage = request.getUserMessage();
        String workflowContent = request.getWorkflowContent();
        Map<String, Object> agentConfig = getRuntimeAgentConfig(executionContext);
        Map<String, Object> agentScope = new HashMap<>(executionContext.getAgentScope());

        // 从 ToolRegistry 获取工具列表
        List<Tool> todoTools = toolRegistry.getToolsByClass(TodoWriteTool.class);
        List<Tool> statusTools = toolRegistry.getToolsByClass(StatusUpdateTool.class);

        // 合并所有工具
        List<Tool> allTools = new ArrayList<>();
        allTools.addAll(todoTools);
        allTools.addAll(statusTools);
        if (executionContext.getClientTools() != null && !executionContext.getClientTools().isEmpty()) {
            allTools.addAll(executionContext.getClientTools());
        }

        log.info("为 WorkflowAgent 加载了 {} 个工具", allTools.size());

        // 将工具列表保存到执行上下文（后续调用 LLM 时会用到）
        executionContext.setAllTools(allTools);

        // 创建流式聊天模型（工具规范将在调用时通过 ChatRequest 传递）
        StreamingChatModel streamingModel = streamingChatModelFactory.createStreamingChatModel(
                (String) agentConfig.get("llmModelIdentifier"),
                agentConfig
        );

        agentCallback.addMemoryMessage(SystemMessage.from(WorkflowAgentPrompts.SYSTEM_PROMPT), (chatMessage) -> {
            // 保存系统消息
            org.joker.comfypilot.session.domain.entity.ChatMessage systemChatMessage = org.joker.comfypilot.session.domain.entity.ChatMessage.builder()
                    .sessionId(executionContext.getSessionId())
                    .sessionCode(executionContext.getSessionCode())
                    .requestId(executionContext.getRequestId())
                    .role(MessageRole.SYSTEM)
                    .status(MessageStatus.ACTIVE)
                    .metadata(new HashMap<>())
                    .content("")
                    .chatContent(PersistableChatMessage.toJsonString(chatMessage))
                    .build();
            chatMessageRepository.save(systemChatMessage);
        }, null);


        ChatRequest chatRequest = ChatRequest.builder()
                .toolSpecifications(allTools.stream().map(Tool::toolSpecification).toList())
                .messages(agentCallback.getMemoryMessages())
                .build();


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
