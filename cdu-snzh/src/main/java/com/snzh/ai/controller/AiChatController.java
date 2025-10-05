package com.snzh.ai.controller;

import com.snzh.ai.domain.dto.AiChatRequestDTO;
import com.snzh.ai.domain.vo.AiChatResponseVO;
import com.snzh.ai.domain.vo.AiSessionVO;
import com.snzh.ai.service.IAiChatService;
import com.snzh.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author haibara
 * @description AI智能客服控制器
 * @since 2025/10/5
 */
@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
@Tag(name = "AI智能客服", description = "AI智能客服相关接口")
public class AiChatController {

    private final IAiChatService aiChatService;

    /**
     * AI对话接口（同步阻塞版）
     */
    @PostMapping
    @Operation(summary = "AI对话（同步）", description = "发送消息给AI客服，获取完整回复（阻塞等待）")
    public ResponseResult<AiChatResponseVO> chat(@Valid @RequestBody AiChatRequestDTO request) {
        log.info("收到AI同步对话请求：userId={}, message={}", request.getUserId(), request.getMessage());
        AiChatResponseVO response = aiChatService.chat(request);
        return ResponseResult.success(response);
    }

    /**
     * AI对话接口（流式响应版）
     */
    @PostMapping("/stream")
    @Operation(summary = "AI对话（流式）", description = "发送消息给AI客服，流式获取回复（推荐使用）")
    public SseEmitter chatStream(@Valid @RequestBody AiChatRequestDTO request) {
        log.info("收到AI流式对话请求：userId={}, message={}", request.getUserId(), request.getMessage());
        
        // 创建SseEmitter，超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        
        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("流式对话超时：userId={}", request.getUserId());
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.info("流式对话完成：userId={}", request.getUserId());
        });
        
        emitter.onError(e -> {
            log.error("流式对话错误：userId={}", request.getUserId(), e);
        });
        
        // 异步处理对话
        aiChatService.chatStream(request, emitter);
        
        return emitter;
    }

    /**
     * 获取用户会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "获取用户的所有会话")
    public ResponseResult<List<AiSessionVO>> getSessions(@RequestParam Long userId) {
        List<AiSessionVO> sessions = aiChatService.getUserSessions(userId);
        return ResponseResult.success(sessions);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定会话（软删除）")
    public ResponseResult<Boolean> deleteSession(
            @PathVariable String sessionId,
            @RequestParam Long userId) {
        Boolean result = aiChatService.deleteSession(sessionId, userId);
        return ResponseResult.success(result);
    }
}

