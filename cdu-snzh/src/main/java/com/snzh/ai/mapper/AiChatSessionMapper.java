package com.snzh.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snzh.ai.domain.entity.AiChatSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author haibara
 * @description AI客服会话Mapper
 * @since 2025/10/5
 */
@Mapper
public interface AiChatSessionMapper extends BaseMapper<AiChatSession> {
}

