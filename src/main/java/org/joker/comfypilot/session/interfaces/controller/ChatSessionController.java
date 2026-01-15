package org.joker.comfypilot.session.interfaces.controller;

import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.session.application.dto.*;
import org.joker.comfypilot.session.application.service.ChatSessionApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 */
@RestController
@RequestMapping("/api/v1/sessions")
public class ChatSessionController {

    @Autowired
    private ChatSessionApplicationService chatSessionApplicationService;

    /**
     * 获取会话列表
     */
    @GetMapping
    public Result<List<ChatSessionDTO>> list(@RequestParam(required = false) Long workflowId) {
        // TODO: 实现获取会话列表逻辑
        return null;
    }

    /**
     * 根据ID获取会话
     */
    @GetMapping("/{id}")
    public Result<ChatSessionDTO> getById(@PathVariable Long id) {
        // TODO: 实现根据ID获取会话逻辑
        return null;
    }

    /**
     * 创建会话
     */
    @PostMapping
    public Result<ChatSessionDTO> create(@RequestBody ChatSessionCreateRequest request) {
        // TODO: 实现创建会话逻辑
        return null;
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/{id}/messages")
    public Result<List<ChatMessageDTO>> getMessages(@PathVariable Long id) {
        // TODO: 实现获取会话消息列表逻辑
        return null;
    }

    /**
     * 发送消息
     */
    @PostMapping("/{id}/messages")
    public Result<ChatMessageDTO> sendMessage(@PathVariable Long id, @RequestBody ChatMessageSendRequest request) {
        // TODO: 实现发送消息逻辑
        return null;
    }
}
