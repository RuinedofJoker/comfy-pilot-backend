package org.joker.comfypilot.cfsvr.application.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContext;
import org.joker.comfypilot.agent.domain.context.AgentExecutionContextHolder;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIClientFactory;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIRestClient;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.QueueStatusResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.SystemStatsResponse;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.exception.BusinessException;
import org.joker.comfypilot.session.application.dto.ChatSessionDTO;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * ComfyUI 服务工具类
 * 提供基于 serverKey 的 ComfyUI 服务调用功能
 */
@Slf4j
@Component
@ToolSet("comfyui_server_")
public class ComfyUIServerTool {

    @Autowired
    private ComfyUIClientFactory clientFactory;

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 获取 ComfyUI 服务器系统状态
     *
     * @return 系统状态信息的 JSON 字符串
     */
    @Tool("获取当前用户连接的 ComfyUI 服务器的系统状态，包括版本信息、系统资源使用情况等")
    public String getSystemStats() {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(executionContext.getSessionCode());
        log.info("调用工具: getSystemStats, serverId: {}", chatSessionDTO.getComfyuiServerId());
        ComfyUIRestClient client = clientFactory.createRestClient(chatSessionDTO.getComfyuiServerId());
        SystemStatsResponse response = client.getSystemStats();
        return response != null ? response.toString() : "获取系统状态失败";
    }

    /**
     * 获取 ComfyUI 任务队列状态
     *
     * @return 队列状态信息的 JSON 字符串
     */
    @Tool("获取当前用户连接的 ComfyUI 服务器的任务队列状态，包括正在运行的任务和等待中的任务")
    public String getQueueStatus() {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(executionContext.getSessionCode());
        log.info("调用工具: getQueueStatus, serverId: {}", chatSessionDTO.getComfyuiServerId());
        ComfyUIRestClient client = clientFactory.createRestClient(chatSessionDTO.getComfyuiServerId());
        QueueStatusResponse response = client.getQueueStatus();
        return response != null ? response.toString() : "获取队列状态失败";
    }

    /**
     * 获取可用的模型文件夹列表
     *
     * @return 模型文件夹列表的 JSON 字符串
     */
    @Tool("获取当前用户连接的 ComfyUI 服务器上可用的模型文件夹列表")
    public String getModelFolders() {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(executionContext.getSessionCode());
        log.info("调用工具: getModelFolders, serverId: {}", chatSessionDTO.getComfyuiServerId());
        ComfyUIRestClient client = clientFactory.createRestClient(chatSessionDTO.getComfyuiServerId());
        List<String> folders = client.getModelFolders();
        return folders != null ? String.join(", ", folders) : "获取模型文件夹列表失败";
    }

    /**
     * 获取指定文件夹的模型列表
     *
     * @param folder 文件夹名称
     * @return 模型列表的 JSON 字符串
     */
    @Tool("获取当前用户连接的 ComfyUI 服务器指定文件夹中的模型列表")
    public String getModels(String folder) {
        AgentExecutionContext executionContext = AgentExecutionContextHolder.get();
        if (executionContext == null) {
            throw new BusinessException("找不到当前工具执行上下文");
        }
        ChatSessionDTO chatSessionDTO = chatSessionService.getSessionByCode(executionContext.getSessionCode());
        log.info("调用工具: getModels, serverId: {}", chatSessionDTO.getComfyuiServerId());
        ComfyUIRestClient client = clientFactory.createRestClient(chatSessionDTO.getComfyuiServerId());
        List<String> models = client.getModels(folder);
        return models != null ? String.join(", ", models) : "获取模型列表失败";
    }
}
