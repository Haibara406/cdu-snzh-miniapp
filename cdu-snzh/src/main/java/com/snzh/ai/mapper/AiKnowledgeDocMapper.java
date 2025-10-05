package com.snzh.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.snzh.ai.domain.entity.AiKnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author haibara
 * @description AI知识库文档Mapper
 * @since 2025/10/5
 */
@Mapper
public interface AiKnowledgeDocMapper extends BaseMapper<AiKnowledgeDoc> {
}

