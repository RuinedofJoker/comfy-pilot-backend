package org.joker.comfypilot.session.interfaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.joker.comfypilot.auth.infrastructure.context.UserContextHolder;
import org.joker.comfypilot.common.interfaces.response.Result;
import org.joker.comfypilot.session.application.dto.*;
import org.joker.comfypilot.session.application.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话控制器
 */
@Tag(name = "会话管理", description = "会话创建、消息发送、历史查询相关接口")
@RestController
@RequestMapping("/api/v1/sessions")
public class ChatSessionController {

    @Lazy
    @Autowired
    private ChatSessionService chatSessionService;

    @Operation(summary = "创建会话", description = "创建一个新的对话会话（返回会话编码）")
    @PostMapping
    public Result<String> createSession(@RequestBody CreateSessionRequest request) {
        Long userId = UserContextHolder.getCurrentUserId();
        String sessionCode = chatSessionService.createSession(userId, request);
        return Result.success(sessionCode);
    }

    @Operation(summary = "编辑会话", description = "编辑对话会话")
    @PutMapping("/{sessionCode}")
    public Result<String> updateSession(
            @Parameter(description = "会话编码", required = true) @PathVariable String sessionCode,
            @RequestBody UpdateSessionRequest request
    ) {
        Long userId = UserContextHolder.getCurrentUserId();
        sessionCode = chatSessionService.updateSession(userId, sessionCode, request);
        return Result.success(sessionCode);
    }

    @Operation(summary = "查询会话详情", description = "根据会话编码查询会话详情")
    @GetMapping("/{sessionCode}")
    public Result<ChatSessionDTO> getSession(
            @Parameter(description = "会话编码", required = true)
            @PathVariable String sessionCode
    ) {
        ChatSessionDTO session = chatSessionService.getSessionByCode(sessionCode);
        return Result.success(session);
    }

    @Operation(summary = "查询用户会话列表", description = "查询当前用户的所有会话")
    @GetMapping
    public Result<List<ChatSessionDTO>> getUserSessions() {
        Long userId = UserContextHolder.getCurrentUserId();
        List<ChatSessionDTO> sessions = chatSessionService.getSessionsByUserId(userId);
        return Result.success(sessions);
    }

    @Operation(summary = "查询用户活跃会话列表", description = "查询当前用户的所有活跃会话，支持按ComfyUI服务过滤")
    @GetMapping("/active")
    public Result<List<ChatSessionDTO>> getUserActiveSessions(
            @Parameter(description = "ComfyUI服务ID") @RequestParam(required = false) Long comfyuiServerId
    ) {
        Long userId = UserContextHolder.getCurrentUserId();
        List<ChatSessionDTO> sessions = chatSessionService.getActiveSessionsByUserIdAndComfyuiServerId(userId, comfyuiServerId);
        return Result.success(sessions);
    }

    @Operation(summary = "查询消息历史", description = "查询会话的所有消息历史")
    @GetMapping("/{sessionCode}/messages")
    public Result<List<ChatMessageDTO>> getMessageHistory(
            @Parameter(description = "会话编码", required = true)
            @PathVariable String sessionCode) {
        List<ChatMessageDTO> messages = chatSessionService.getClientMessageHistory(sessionCode);
        return Result.success(messages);
    }

    @Operation(summary = "归档会话", description = "归档指定的会话")
    @PostMapping("/{sessionCode}/archive")
    public Result<Void> archiveSession(
            @Parameter(description = "会话编码", required = true)
            @PathVariable String sessionCode) {
        chatSessionService.archiveSession(sessionCode);
        return Result.success(null);
    }
}
