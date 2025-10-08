package com.snzh.ai.controller;

import com.snzh.ai.domain.dto.AiChatRequestDTO;
import com.snzh.ai.domain.vo.AiChatResponseVO;
import com.snzh.ai.domain.vo.AiSessionVO;
import com.snzh.ai.service.IAiChatService;
import com.snzh.domain.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author haibara
 * @description AI智能客服测试控制器（无需登录，用于测试）
 * @since 2025/10/8
 */
@Slf4j
@RestController
@RequestMapping("/ai/test")
@RequiredArgsConstructor
@Tag(name = "AI智能客服测试接口", description = "无需登录的AI测试接口，仅用于开发和测试")
public class AiTestController {

    private final IAiChatService aiChatService;

    /**
     * 默认测试用户ID（用于测试环境）
     */
    private static final Long TEST_USER_ID = 999999L;

    /**
     * AI对话测试接口（同步阻塞版）- 简化版
     */
    @PostMapping("/chat")
    @Operation(summary = "AI对话测试（同步）", description = "测试专用，无需登录，使用默认测试用户ID")
    public ResponseResult<AiChatResponseVO> testChat(
            @Parameter(description = "用户消息") 
            @RequestParam String message,
            @Parameter(description = "会话ID（可选）") 
            @RequestParam(required = false) String sessionId) {
        
        log.info("收到AI测试对话请求：message={}, sessionId={}", message, sessionId);
        
        AiChatRequestDTO request = new AiChatRequestDTO();
        request.setMessage(message);
        request.setUserId(TEST_USER_ID);
        request.setSessionId(sessionId);
        
        AiChatResponseVO response = aiChatService.chat(request);
        return ResponseResult.success(response);
    }

    /**
     * AI对话测试接口（同步阻塞版）- 完整版
     */
    @PostMapping("/chat/full")
    @Operation(summary = "AI对话测试（同步-完整）", description = "测试专用，无需登录，可指定用户ID")
    public ResponseResult<AiChatResponseVO> testChatFull(
            @Parameter(description = "用户消息") 
            @RequestParam String message,
            @Parameter(description = "用户ID（可选，默认使用测试用户ID）") 
            @RequestParam(required = false) Long userId,
            @Parameter(description = "会话ID（可选）") 
            @RequestParam(required = false) String sessionId) {
        
        Long actualUserId = userId != null ? userId : TEST_USER_ID;
        log.info("收到AI测试对话请求：userId={}, message={}, sessionId={}", actualUserId, message, sessionId);
        
        AiChatRequestDTO request = new AiChatRequestDTO();
        request.setMessage(message);
        request.setUserId(actualUserId);
        request.setSessionId(sessionId);
        
        AiChatResponseVO response = aiChatService.chat(request);
        return ResponseResult.success(response);
    }

    /**
     * AI对话测试接口（流式响应版）- 简化版
     */
    @GetMapping("/chat/stream")
    @Operation(summary = "AI对话测试（流式）", description = "测试专用，无需登录，流式获取回复")
    public SseEmitter testChatStream(
            @Parameter(description = "用户消息") 
            @RequestParam String message,
            @Parameter(description = "会话ID（可选）") 
            @RequestParam(required = false) String sessionId) {
        
        log.info("收到AI测试流式对话请求：message={}, sessionId={}", message, sessionId);
        
        AiChatRequestDTO request = new AiChatRequestDTO();
        request.setMessage(message);
        request.setUserId(TEST_USER_ID);
        request.setSessionId(sessionId);
        
        // 创建SseEmitter，超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        
        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("测试流式对话超时");
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.info("测试流式对话完成");
        });
        
        emitter.onError(e -> {
            log.error("测试流式对话错误", e);
        });
        
        // 异步处理对话
        aiChatService.chatStream(request, emitter);
        
        return emitter;
    }

    /**
     * AI对话测试接口（流式响应版）- 完整版
     */
    @GetMapping("/chat/stream/full")
    @Operation(summary = "AI对话测试（流式-完整）", description = "测试专用，无需登录，流式获取回复，可指定用户ID")
    public SseEmitter testChatStreamFull(
            @Parameter(description = "用户消息") 
            @RequestParam String message,
            @Parameter(description = "用户ID（可选，默认使用测试用户ID）") 
            @RequestParam(required = false) Long userId,
            @Parameter(description = "会话ID（可选）") 
            @RequestParam(required = false) String sessionId) {
        
        Long actualUserId = userId != null ? userId : TEST_USER_ID;
        log.info("收到AI测试流式对话请求：userId={}, message={}, sessionId={}", actualUserId, message, sessionId);
        
        AiChatRequestDTO request = new AiChatRequestDTO();
        request.setMessage(message);
        request.setUserId(actualUserId);
        request.setSessionId(sessionId);
        
        // 创建SseEmitter，超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        
        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("测试流式对话超时：userId={}", actualUserId);
            emitter.complete();
        });
        
        emitter.onCompletion(() -> {
            log.info("测试流式对话完成：userId={}", actualUserId);
        });
        
        emitter.onError(e -> {
            log.error("测试流式对话错误：userId={}", actualUserId, e);
        });
        
        // 异步处理对话
        aiChatService.chatStream(request, emitter);
        
        return emitter;
    }

    /**
     * 获取测试用户的会话列表
     */
    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "获取测试用户的所有会话")
    public ResponseResult<List<AiSessionVO>> getTestSessions(
            @Parameter(description = "用户ID（可选，默认使用测试用户ID）") 
            @RequestParam(required = false) Long userId) {
        
        Long actualUserId = userId != null ? userId : TEST_USER_ID;
        log.info("获取测试用户会话列表：userId={}", actualUserId);
        
        List<AiSessionVO> sessions = aiChatService.getUserSessions(actualUserId);
        return ResponseResult.success(sessions);
    }

    /**
     * 删除测试会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定测试会话（软删除）")
    public ResponseResult<Boolean> deleteTestSession(
            @PathVariable String sessionId,
            @Parameter(description = "用户ID（可选，默认使用测试用户ID）") 
            @RequestParam(required = false) Long userId) {
        
        Long actualUserId = userId != null ? userId : TEST_USER_ID;
        log.info("删除测试会话：sessionId={}, userId={}", sessionId, actualUserId);
        
        Boolean result = aiChatService.deleteSession(sessionId, actualUserId);
        return ResponseResult.success(result);
    }

    /**
     * 获取当前测试用户ID
     */
    @GetMapping("/user-id")
    @Operation(summary = "获取测试用户ID", description = "返回当前默认的测试用户ID")
    public ResponseResult<Long> getTestUserId() {
        log.info("获取测试用户ID：{}", TEST_USER_ID);
        return ResponseResult.success(TEST_USER_ID);
    }
}

