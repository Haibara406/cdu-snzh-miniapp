package com.snzh.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snzh.ai.domain.entity.AiChatMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author haibara
 * @description AI客服消息Mapper
 * @since 2025/10/5
 */
@Mapper
public interface AiChatMessageMapper extends BaseMapper<AiChatMessage> {
}

