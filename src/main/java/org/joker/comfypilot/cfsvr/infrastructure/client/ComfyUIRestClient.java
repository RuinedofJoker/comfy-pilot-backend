package org.joker.comfypilot.cfsvr.infrastructure.client;

import org.joker.comfypilot.cfsvr.infrastructure.client.dto.PromptRequest;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.PromptResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.QueueStatusResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.SystemStatsResponse;

import java.util.List;
import java.util.Map;

/**
 * ComfyUI REST客户端接口
 */
public interface ComfyUIRestClient {

    /**
     * 获取系统状态
     *
     * @return 系统状态信息
     */
    SystemStatsResponse getSystemStats();

    /**
     * 获取队列状态
     *
     * @return 队列状态信息
     */
    QueueStatusResponse getQueueStatus();

    /**
     * 提交工作流执行请求
     *
     * @param request 工作流请求
     * @return 执行响应
     */
    PromptResponse submitPrompt(PromptRequest request);

    /**
     * 获取模型文件夹列表
     *
     * @return 模型文件夹列表
     */
    List<String> getModelFolders();

    /**
     * 获取指定文件夹的模型列表
     *
     * @param folder 文件夹名称
     * @return 模型列表
     */
    List<String> getModels(String folder);

    /**
     * 获取历史记录
     *
     * @return 历史记录
     */
    Map<String, Object> getHistory();

    /**
     * 获取特定任务的历史记录
     *
     * @param promptId 任务ID
     * @return 任务历史记录
     */
    Map<String, Object> getHistoryByPromptId(String promptId);
}
