package com.snzh.ai.service;

import com.snzh.ai.domain.entity.AiKnowledgeDoc;

import java.util.List;

/**
 * @author haibara
 * @description 知识库服务接口
 * @since 2025/10/5
 */
public interface IKnowledgeBaseService {

    /**
     * 加载所有知识库文档并向量化
     */
    void loadAndIndexDocuments();

    /**
     * 根据查询检索相关知识
     *
     * @param query      查询文本
     * @param maxResults 最大结果数
     * @return 相关知识列表
     */
    List<String> searchRelevantKnowledge(String query, int maxResults);

    /**
     * 添加知识库文档
     *
     * @param doc 文档
     * @return 是否成功
     */
    Boolean addDocument(AiKnowledgeDoc doc);

    /**
     * 重建索引
     */
    void rebuildIndex();
}

