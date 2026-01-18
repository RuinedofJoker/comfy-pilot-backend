package org.joker.comfypilot.cfsvr.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.PromptRequest;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.PromptResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.QueueStatusResponse;
import org.joker.comfypilot.cfsvr.infrastructure.client.dto.SystemStatsResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * ComfyUI REST客户端实现
 */
@Slf4j
public class ComfyUIRestClientImpl implements ComfyUIRestClient {

    private final WebClient webClient;
    private final Duration timeout;

    /**
     * 构造函数
     *
     * @param webClient WebClient实例
     * @param timeout   请求超时时间
     */
    public ComfyUIRestClientImpl(WebClient webClient, Duration timeout) {
        this.webClient = webClient;
        this.timeout = timeout;
    }

    @Override
    public SystemStatsResponse getSystemStats() {
        log.debug("调用ComfyUI接口: GET /system_stats");

        return webClient.get()
                .uri("/system_stats")
                .retrieve()
                .bodyToMono(SystemStatsResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("获取系统状态成功"))
                .doOnError(error -> log.error("获取系统状态失败", error))
                .block();
    }

    @Override
    public QueueStatusResponse getQueueStatus() {
        log.debug("调用ComfyUI接口: GET /queue");

        return webClient.get()
                .uri("/queue")
                .retrieve()
                .bodyToMono(QueueStatusResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("获取队列状态成功"))
                .doOnError(error -> log.error("获取队列状态失败", error))
                .block();
    }

    @Override
    public PromptResponse submitPrompt(PromptRequest request) {
        log.debug("调用ComfyUI接口: POST /prompt");

        return webClient.post()
                .uri("/prompt")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PromptResponse.class)
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("提交工作流成功, promptId: {}", response.getPromptId()))
                .doOnError(error -> log.error("提交工作流失败", error))
                .block();
    }

    @Override
    public List<String> getModelFolders() {
        log.debug("调用ComfyUI接口: GET /models");

        return webClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("获取模型文件夹列表成功, 数量: {}", response.size()))
                .doOnError(error -> log.error("获取模型文件夹列表失败", error))
                .block();
    }

    @Override
    public List<String> getModels(String folder) {
        log.debug("调用ComfyUI接口: GET /models/{}", folder);

        return webClient.get()
                .uri("/models/{folder}", folder)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("获取模型列表成功, 文件夹: {}, 数量: {}", folder, response.size()))
                .doOnError(error -> log.error("获取模型列表失败, 文件夹: {}", folder, error))
                .block();
    }

    @Override
    public Map<String, Object> getHistory() {
        log.debug("调用ComfyUI接口: GET /history");

        return webClient.get()
                .uri("/history")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("获取历史记录成功"))
                .doOnError(error -> log.error("获取历史记录失败", error))
                .block();
    }

    @Override
    public Map<String, Object> getHistoryByPromptId(String promptId) {
        log.debug("调用ComfyUI接口: GET /history/{}", promptId);

        return webClient.get()
                .uri("/history/{promptId}", promptId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .timeout(timeout)
                .doOnSuccess(response -> log.debug("获取任务历史记录成功, promptId: {}", promptId))
                .doOnError(error -> log.error("获取任务历史记录失败, promptId: {}", promptId, error))
                .block();
    }
}
