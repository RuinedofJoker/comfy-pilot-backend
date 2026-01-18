package org.joker.comfypilot.agent.domain.agent.workflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.application.dto.AgentExecutionRequest;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.service.AbstractAgent;
import org.joker.comfypilot.agent.domain.service.Agent;
import org.joker.comfypilot.model.domain.repository.ModelProviderRepository;
import org.joker.comfypilot.model.domain.service.StreamingChatModelFactory;
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
    private ModelProviderRepository modelProviderRepository;
    @Autowired
    private StreamingChatModelFactory streamingChatModelFactory;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
    public Map<String, Object> getAgentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("temperature", 0.7);
        config.put("maxTokens", 20000);
        return config;
    }

    @Override
    public Map<String, Object> getAgentScopeConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("SystemPrompt", "你是一个友好的AI助手，专注于回答用户的问题。请保持回答简洁、准确、有帮助。");
        return config;
    }

    protected void executeWithStreaming(AgentExecutionContext executionContext) throws Exception {
        // 发送ai思考中消息
        executionContext.getStreamCallback().onThinking();

        AgentExecutionRequest request = executionContext.getRequest();

        Map<String, Object> input = OBJECT_MAPPER.readValue(request.getInput(), new TypeReference<>() {
        });

        Map<String, Object> agentConfig = new HashMap<>(executionContext.getAgentConfig());
        Map<String, Object> agentScope = new HashMap<>(executionContext.getAgentScope());
        StreamingChatModel streamingModel = streamingChatModelFactory.createStreamingChatModel((String) input.get("llmModel"), agentConfig);

        SystemMessage systemMessage = null;
        LinkedList<ChatMessage> messages = new LinkedList<>();
        // TODO 查询历史消息填充
        // TODO 构建
    }
}
