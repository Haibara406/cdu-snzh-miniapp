package com.snzh.ai.service;

import com.snzh.ai.domain.dto.AiChatRequestDTO;
import com.snzh.ai.domain.vo.AiChatResponseVO;
import com.snzh.ai.domain.vo.AiSessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * @author haibara
 * @description AI对话服务接口
 * @since 2025/10/5
 */
public interface IAiChatService {

    /**
     * 处理用户对话（同步阻塞版）
     *
     * @param request 对话请求
     * @return 对话响应
     */
    AiChatResponseVO chat(AiChatRequestDTO request);

    /**
     * 处理用户对话（流式响应）
     *
     * @param request 对话请求
     * @param emitter SSE发送器
     */
    void chatStream(AiChatRequestDTO request, SseEmitter emitter);

    /**
     * 获取用户的会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<AiSessionVO> getUserSessions(Long userId);

    /**
     * 删除会话（软删除）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 是否成功
     */
    Boolean deleteSession(String sessionId, Long userId);
}

