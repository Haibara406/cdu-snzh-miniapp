package com.snzh.ai.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snzh.ai.domain.entity.AiChatSession;
import com.snzh.ai.mapper.AiChatSessionMapper;
import com.snzh.enums.StatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author haibara
 * @description AI会话清理定时任务
 * @since 2025/10/5
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiSessionCleanupTask {

    private final AiChatSessionMapper sessionMapper;

    /**
     * 每天凌晨3点清理两周前未更新的会话
     * 将status设置为0（禁用），用户不再能看到这些会话
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOldSessions() {
        try {
            log.info("开始清理旧会话...");

            // 计算两周前的时间点
            LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

            // 查询两周前未更新且状态为正常的会话
            LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AiChatSession::getStatus, StatusEnum.RUN.getCode())
                    .lt(AiChatSession::getUpdateTime, twoWeeksAgo);

            List<AiChatSession> oldSessions = sessionMapper.selectList(wrapper);

            if (oldSessions.isEmpty()) {
                log.info("没有需要清理的旧会话");
                return;
            }

            // 批量更新状态为禁用
            for (AiChatSession session : oldSessions) {
                session.setStatus(StatusEnum.STOP.getCode());
            }

            int count = 0;
            for (AiChatSession session : oldSessions) {
                int result = sessionMapper.updateById(session);
                if (result > 0) {
                    count++;
                }
            }

            log.info("清理旧会话完成，共清理{}条记录", count);

        } catch (Exception e) {
            log.error("清理旧会话失败", e);
        }
    }

    /**
     * 手动触发清理（测试用）
     * 可以通过管理端接口调用
     */
    public int manualCleanup() {
        log.info("手动触发清理旧会话");

        LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);

        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiChatSession::getStatus, StatusEnum.RUN.getCode())
                .lt(AiChatSession::getUpdateTime, twoWeeksAgo);

        List<AiChatSession> oldSessions = sessionMapper.selectList(wrapper);

        if (oldSessions.isEmpty()) {
            log.info("没有需要清理的旧会话");
            return 0;
        }

        for (AiChatSession session : oldSessions) {
            session.setStatus(StatusEnum.STOP.getCode());
        }

        int count = 0;
        for (AiChatSession session : oldSessions) {
            int result = sessionMapper.updateById(session);
            if (result > 0) {
                count++;
            }
        }

        log.info("手动清理完成，共清理{}条记录", count);
        return count;
    }
}

