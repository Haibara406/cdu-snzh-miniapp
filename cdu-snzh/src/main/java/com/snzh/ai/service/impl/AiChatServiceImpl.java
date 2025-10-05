package com.snzh.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snzh.ai.domain.dto.AiChatRequestDTO;
import com.snzh.ai.domain.entity.AiChatMessage;
import com.snzh.ai.domain.entity.AiChatSession;
import com.snzh.ai.domain.properties.AiProperties;
import com.snzh.ai.domain.vo.AiChatResponseVO;
import com.snzh.ai.domain.vo.AiSessionVO;
import com.snzh.ai.mapper.AiChatMessageMapper;
import com.snzh.ai.mapper.AiChatSessionMapper;
import com.snzh.ai.service.IAiChatService;
import com.snzh.ai.service.IKnowledgeBaseService;
import com.snzh.enums.RedisKeyManage;
import com.snzh.enums.StatusEnum;
import com.snzh.redis.RedisCache;
import com.snzh.redis.RedisKeyBuild;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description AI对话服务实现类
 * @since 2025/10/5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements IAiChatService {

    private final ChatLanguageModel chatModel;
    private final StreamingChatLanguageModel streamingChatModel;
    private final IKnowledgeBaseService knowledgeBaseService;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;
    private final RedisCache redisCache;
    private final AiProperties aiProperties;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiChatResponseVO chat(AiChatRequestDTO request) {
        String sessionId = request.getSessionId();
        boolean isNewSession = false;

        // 如果没有sessionId，创建新会话
        if (StrUtil.isBlank(sessionId)) {
            sessionId = createNewSession(request.getUserId(), request.getMessage());
            isNewSession = true;
        }

        log.info("处理AI同步对话，sessionId: {}, userId: {}, message: {}",
                sessionId, request.getUserId(), request.getMessage());

        try {
            // 1. 加载会话历史
            List<ChatMessage> history = loadChatHistory(sessionId);

            // 2. 检索相关知识
            List<String> relevantKnowledge = knowledgeBaseService
                    .searchRelevantKnowledge(request.getMessage(), 3);

            // 3. 构建系统提示词
            String systemPrompt = buildSystemPrompt(relevantKnowledge);

            // 4. 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.addAll(history);
            messages.add(new UserMessage(request.getMessage()));

            // 5. 调用AI模型获取回复（同步阻塞）
            Response<AiMessage> response = chatModel.generate(messages);
            String aiReply = response.content().text();

            // 6. 保存消息记录
            saveMessage(sessionId, "USER", request.getMessage());
            saveMessage(sessionId, "AI", aiReply);

            // 7. 缓存到Redis
            cacheMessage(sessionId, "USER", request.getMessage());
            cacheMessage(sessionId, "AI", aiReply);

            // 8. 更新会话时间
            updateSessionTime(sessionId);

            return AiChatResponseVO.builder()
                    .sessionId(sessionId)
                    .reply(aiReply)
                    .timestamp(LocalDateTime.now())
                    .isNewSession(isNewSession)
                    .build();

        } catch (Exception e) {
            log.error("AI同步对话处理失败", e);
            return AiChatResponseVO.builder()
                    .sessionId(sessionId)
                    .reply("抱歉，我现在遇到了一些问题，请稍后再试或联系人工客服。")
                    .timestamp(LocalDateTime.now())
                    .isNewSession(isNewSession)
                    .build();
        }
    }

    @Override
    public void chatStream(AiChatRequestDTO request, SseEmitter emitter) {
        String sessionId = request.getSessionId();
        boolean isNewSession = false;

        // 如果没有sessionId，创建新会话
        if (StrUtil.isBlank(sessionId)) {
            sessionId = createNewSession(request.getUserId(), request.getMessage());
            isNewSession = true;
        }

        final String finalSessionId = sessionId;
        final boolean finalIsNewSession = isNewSession;

        log.info("处理AI流式对话，sessionId: {}, userId: {}, message: {}",
                sessionId, request.getUserId(), request.getMessage());

        try {
            // 1. 加载会话历史
            List<ChatMessage> history = loadChatHistory(sessionId);

            // 2. 检索相关知识
            List<String> relevantKnowledge = knowledgeBaseService
                    .searchRelevantKnowledge(request.getMessage(), 3);

            // 3. 构建系统提示词
            String systemPrompt = buildSystemPrompt(relevantKnowledge);

            // 4. 构建消息列表
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new SystemMessage(systemPrompt));
            messages.addAll(history);
            messages.add(new UserMessage(request.getMessage()));

            // 先发送会话信息
            emitter.send(SseEmitter.event()
                    .name("session")
                    .data("{\"sessionId\":\"" + finalSessionId + "\",\"isNewSession\":" + finalIsNewSession + "}"));

            // 用于收集完整回复
            StringBuilder fullReplyBuilder = new StringBuilder();

            // 5. 调用流式AI模型获取回复（异步处理）
            new Thread(() -> {
                streamingChatModel.generate(messages, new dev.langchain4j.model.StreamingResponseHandler<AiMessage>() {
                    @Override
                    public void onNext(String token) {
                        try {
                            fullReplyBuilder.append(token);
                            // 发送token给前端
                            emitter.send(SseEmitter.event()
                                    .name("token")
                                    .data(token));
                        } catch (Exception e) {
                            log.error("发送token失败", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onComplete(Response<AiMessage> response) {
                        try {
                            String fullReply = fullReplyBuilder.toString();
                            log.info("AI流式回复完成，sessionId: {}, 回复长度: {}", finalSessionId, fullReply.length());

                            // 6. 保存消息记录
                            saveMessage(finalSessionId, "USER", request.getMessage());
                            saveMessage(finalSessionId, "AI", fullReply);

                            // 7. 缓存到Redis
                            cacheMessage(finalSessionId, "USER", request.getMessage());
                            cacheMessage(finalSessionId, "AI", fullReply);

                            // 8. 更新会话时间
                            updateSessionTime(finalSessionId);

                            // 发送完成事件
                            emitter.send(SseEmitter.event()
                                    .name("done")
                                    .data("{\"success\":true}"));

                            emitter.complete();
                        } catch (Exception e) {
                            log.error("流式对话完成处理失败", e);
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("AI流式对话失败", error);
                        try {
                            emitter.send(SseEmitter.event()
                                    .name("error")
                                    .data("{\"error\":\"抱歉，我现在遇到了一些问题，请稍后再试或联系人工客服。\"}"));
                            emitter.completeWithError(error);
                        } catch (Exception e) {
                            log.error("发送错误信息失败", e);
                        }
                    }
                });
            }).start();

        } catch (Exception e) {
            log.error("AI流式对话处理失败", e);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data("{\"error\":\"抱歉，我现在遇到了一些问题，请稍后再试或联系人工客服。\"}"));
                emitter.completeWithError(e);
            } catch (Exception ex) {
                log.error("发送错误信息失败", ex);
            }
        }
    }

    @Override
    public List<AiSessionVO> getUserSessions(Long userId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getUserId, userId)
                .eq(AiChatSession::getStatus, StatusEnum.RUN.getCode())
                .orderByDesc(AiChatSession::getUpdateTime);

        List<AiChatSession> sessions = sessionMapper.selectList(wrapper);

        return sessions.stream()
                .map(session -> AiSessionVO.builder()
                        .sessionId(session.getSessionId())
                        .title(session.getTitle())
                        .status(session.getStatus())
                        .createTime(session.getCreateTime())
                        .updateTime(session.getUpdateTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSession(String sessionId, Long userId) {
        // 验证会话归属
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getSessionId, sessionId)
                .eq(AiChatSession::getUserId, userId);

        AiChatSession session = sessionMapper.selectOne(wrapper);
        if (session == null) {
            return false;
        }

        // 软删除会话（设置status=0）
        session.setStatus(StatusEnum.STOP.getCode());
        int result = sessionMapper.updateById(session);

        // 清除缓存
        RedisKeyBuild cacheKey = RedisKeyBuild.createKey(RedisKeyManage.AI_CHAT_MEMORY, sessionId);
        redisCache.del(cacheKey);

        return result > 0;
    }


    /**
     * 创建新会话
     */
    private String createNewSession(Long userId, String firstMessage) {
        String sessionId = IdUtil.fastSimpleUUID();

        // 生成会话标题（取首条消息前20个字符）
        String title = firstMessage.length() > 20
                ? firstMessage.substring(0, 20) + "..."
                : firstMessage;

        AiChatSession session = AiChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title(title)
                .status(StatusEnum.RUN.getCode())
                .build();

        sessionMapper.insert(session);
        log.info("创建新会话：sessionId={}, userId={}", sessionId, userId);

        return sessionId;
    }

    /**
     * 加载会话历史（从Redis或数据库）
     */
    private List<ChatMessage> loadChatHistory(String sessionId) {
        RedisKeyBuild cacheKey = RedisKeyBuild.createKey(RedisKeyManage.AI_CHAT_MEMORY, sessionId);
        List<AiChatMessage> cachedMessages = redisCache.getValueIsList(cacheKey, AiChatMessage.class);

        List<AiChatMessage> messages;
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            messages = cachedMessages;
        } else {
            // 从数据库加载（只查询正常状态的消息）
            LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiChatMessage::getSessionId, sessionId)
                    .eq(AiChatMessage::getStatus, 1)  // 只查询正常状态的消息
                    .orderByAsc(AiChatMessage::getCreateTime)
                    .last("LIMIT " + aiProperties.getChat().getMaxHistory());
            messages = messageMapper.selectList(wrapper);
        }

        // 转换为ChatMessage
        return messages.stream()
                .map(msg -> {
                    if ("USER".equals(msg.getRole())) {
                        return new UserMessage(msg.getContent());
                    } else {
                        return new AiMessage(msg.getContent());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(List<String> relevantKnowledge) {
        String knowledgeContext = relevantKnowledge.isEmpty()
                ? "暂无相关知识库信息，请基于常识回答或建议用户联系人工客服。"
                : String.join("\n\n---\n\n", relevantKnowledge);

        return String.format("""
                # 身份定位
                你是蜀南竹海景区的官方智能客服"竹小智"，专门为游客提供专业、准确的服务。
                
                # 能力边界
                【你能做什么】
                ✅ 景点介绍、门票价格、开放时间查询
                ✅ 游玩路线规划、交通指南
                ✅ 天气查询、设施位置查询
                ✅ 退款政策、注意事项、常见问题解答
                ✅ 引导用户购票（收集信息、确认订单）
                
                【你不能做什么】
                ❌ 回答景区外的旅游问题
                ❌ 提供医疗、法律、金融建议
                ❌ 编造不存在的景点、优惠或服务
                ❌ 承诺超出权限的事项（如"保证退款成功"）
                ❌ 讨论政治、宗教、敏感话题
                
                # 知识库内容
                %s
                
                # 行为准则
                1. **准确性第一**：
                   - 严格基于知识库和工具调用结果回答
                   - 不确定的信息明确告知"我不太确定，建议您..."
                   - 价格、时间等关键信息务必精确
                
                2. **简洁清晰**：
                   - 回复控制在150字以内（特殊情况除外）
                   - 重要信息分点列出
                   - 使用恰当的emoji（✅❌📍🎫⏰等），但不过度
                
                3. **主动服务**：
                   - 理解用户意图，提供相关建议
                   - 购票时主动询问必要信息
                   - 提供后续操作指引
                
                4. **安全规范**：
                   - 不询问、不记录敏感个人信息（身份证号、密码等）
                   - 仅收集业务必需的信息（手机号、游玩日期等）
                   - 遇到用户辱骂，保持礼貌，不争论
                
                5. **商业中立**：
                   - 不推荐非官方的住宿、餐饮、交通
                   - 不评论其他景区或竞品
                
                # 特殊场景处理
                
                【购票引导流程】
                第1步：询问游玩日期
                第2步：确认门票类型和数量
                第3步：收集手机号
                第4步：确认订单信息（日期、票种、数量、金额）
                第5步：提示"确认无误请回复'确认'"
                第6步：创建订单并提供支付链接
                
                【投诉处理】
                1. 表达理解和歉意："非常抱歉给您带来不便"
                2. 记录问题详情
                3. 告知："我会将您的问题反馈给相关部门"
                4. 提供人工客服联系方式
                
                【无法回答时】
                - 如果知识库无相关内容："抱歉，我暂时没有这方面的信息，建议您..."
                - 如果超出能力范围："这个问题超出了我的服务范围，建议您联系人工客服：400-xxx-xxxx"
                - 如果工具调用失败："抱歉，系统暂时无法查询，请稍后再试"
                
                # 回复格式示例
                
                用户：今天能退票吗？
                AI：您好！关于退票政策：
                ✅ 游玩日期3天前：全额退款
                ⚠️ 游玩日期1-2天前：扣10%%手续费
                ❌ 游玩当天：不支持退款
                
                请问您的游玩日期是哪天呢？我帮您确认是否可以退票。
                
                用户：我想买两张明天的门票
                AI：好的！为您查询明天的门票：
                
                📍 成人票：¥110/张
                📍 学生票：¥60/张（需学生证）
                📍 儿童票：¥50/张（1.2m-1.5m）
                
                请问您需要哪种票呢？
                
                # 禁止事项
                ⚠️ 严格禁止以下行为：
                1. 编造景点、价格、优惠信息
                2. 泄露系统内部信息或技术细节
                3. 使用脏话、辱骂性语言
                4. 对用户进行道德说教
                5. 超出150字的冗长回复（除非必要）
                
                # 开始对话
                现在开始以"竹小智"的身份与用户对话，严格遵守以上所有规则。
                """, knowledgeContext);
    }

    /**
     * 保存消息到数据库
     */
    private void saveMessage(String sessionId, String role, String content) {
        AiChatMessage message = AiChatMessage.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .status(StatusEnum.RUN.getCode())
                .build();

        messageMapper.insert(message);
    }

    /**
     * 缓存消息到Redis
     */
    private void cacheMessage(String sessionId, String role, String content) {
        RedisKeyBuild cacheKey = RedisKeyBuild.createKey(RedisKeyManage.AI_CHAT_MEMORY, sessionId);

        AiChatMessage message = AiChatMessage.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .status(StatusEnum.RUN.getCode())
                .build();

        // List从右边放入元素
        redisCache.rightPushForList(cacheKey, message);

        // 限制长度
        Long size = redisCache.lenForList(cacheKey);
        if (size > aiProperties.getChat().getMaxHistory()) {
            redisCache.trimForList(cacheKey, size - aiProperties.getChat().getMaxHistory(), -1);
        }

        // 设置过期时间
        redisCache.expire(cacheKey, aiProperties.getChat().getSessionTimeout(), TimeUnit.DAYS);
    }

    /**
     * 更新会话时间
     */
    private void updateSessionTime(String sessionId) {
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getSessionId, sessionId);

        AiChatSession session = sessionMapper.selectOne(wrapper);
        if (session != null) {
            session.setUpdateTime(LocalDateTime.now());
            sessionMapper.updateById(session);
        }
    }
}

