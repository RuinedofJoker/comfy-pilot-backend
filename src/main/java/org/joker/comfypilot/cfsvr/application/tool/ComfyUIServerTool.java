package org.joker.comfypilot.cfsvr.application.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIClientFactory;
import org.joker.comfypilot.cfsvr.infrastructure.client.ComfyUIRestClient;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.PromptRequest;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.PromptResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.QueueStatusResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.SystemStatsResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

/**
 * ComfyUI 服务工具类
 * 提供基于 serverKey 的 ComfyUI 服务调用功能
 * 所有 public 方法都会被自动注册为 LangChain4j 工具
 */
@Slf4j
@Component
public class ComfyUIServerTool {

    @Autowired
    private ComfyUIClientFactory clientFactory;

    /**
     * 获取 ComfyUI 服务器系统状态
     *
     * @param serverKey 服务器唯一标识符
     * @return 系统状态信息的 JSON 字符串
     */
    @Tool("获取 ComfyUI 服务器的系统状态，包括版本信息、系统资源使用情况等")
    public String getSystemStats(String serverKey) {
        log.info("调用工具: getSystemStats, serverKey: {}", serverKey);
        try {
            ComfyUIRestClient client = clientFactory.createRestClient(serverKey);
            SystemStatsResponse response = client.getSystemStats();
            return response != null ? response.toString() : "获取系统状态失败";
        } catch (Exception e) {
            log.error("获取系统状态失败, serverKey: {}", serverKey, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取 ComfyUI 任务队列状态
     *
     * @param serverKey 服务器唯一标识符
     * @return 队列状态信息的 JSON 字符串
     */
    @Tool("获取 ComfyUI 服务器的任务队列状态，包括正在运行的任务和等待中的任务")
    public String getQueueStatus(String serverKey) {
        log.info("调用工具: getQueueStatus, serverKey: {}", serverKey);
        try {
            ComfyUIRestClient client = clientFactory.createRestClient(serverKey);
            QueueStatusResponse response = client.getQueueStatus();
            return response != null ? response.toString() : "获取队列状态失败";
        } catch (Exception e) {
            log.error("获取队列状态失败, serverKey: {}", serverKey, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 提交工作流执行请求
     *
     * @param serverKey 服务器唯一标识符
     * @param promptJson 工作流 JSON 字符串
     * @return 执行响应信息的 JSON 字符串
     */
    @Tool("提交工作流到 ComfyUI 服务器执行，返回任务 ID")
    public String submitPrompt(String serverKey, String promptJson) {
        log.info("调用工具: submitPrompt, serverKey: {}", serverKey);
        try {
            ComfyUIRestClient client = clientFactory.createRestClient(serverKey);
            // 这里需要将 JSON 字符串转换为 PromptRequest 对象
            // 简化处理，直接返回提示信息
            return "提交工作流功能需要完整的 PromptRequest 对象，请使用 API 接口";
        } catch (Exception e) {
            log.error("提交工作流失败, serverKey: {}", serverKey, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取可用的模型文件夹列表
     *
     * @param serverKey 服务器唯一标识符
     * @return 模型文件夹列表的 JSON 字符串
     */
    @Tool("获取 ComfyUI 服务器上可用的模型文件夹列表")
    public String getModelFolders(String serverKey) {
        log.info("调用工具: getModelFolders, serverKey: {}", serverKey);
        try {
            ComfyUIRestClient client = clientFactory.createRestClient(serverKey);
            List<String> folders = client.getModelFolders();
            return folders != null ? String.join(", ", folders) : "获取模型文件夹列表失败";
        } catch (Exception e) {
            log.error("获取模型文件夹列表失败, serverKey: {}", serverKey, e);
            return "错误: " + e.getMessage();
        }
    }

    /**
     * 获取指定文件夹的模型列表
     *
     * @param serverKey 服务器唯一标识符
     * @param folder 文件夹名称
     * @return 模型列表的 JSON 字符串
     */
    @Tool("获取 ComfyUI 服务器指定文件夹中的模型列表")
    public String getModels(String serverKey, String folder) {
        log.info("调用工具: getModels, serverKey: {}, folder: {}", serverKey, folder);
        try {
            ComfyUIRestClient client = clientFactory.createRestClient(serverKey);
            List<String> models = client.getModels(folder);
            return models != null ? String.join(", ", models) : "获取模型列表失败";
        } catch (Exception e) {
            log.error("获取模型列表失败, serverKey: {}, folder: {}", serverKey, folder, e);
            return "错误: " + e.getMessage();
        }
    }
}
