package com.snzh.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.snzh.ai.domain.entity.AiKnowledgeDoc;
import com.snzh.ai.mapper.AiKnowledgeDocMapper;
import com.snzh.ai.service.IKnowledgeBaseService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author haibara
 * @description 知识库服务实现类
 * @since 2025/10/5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements IKnowledgeBaseService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final AiKnowledgeDocMapper knowledgeDocMapper;

    /**
     * 应用启动时加载知识库
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化知识库...");
        loadAndIndexDocuments();
        log.info("知识库初始化完成");
    }

    @Override
    public void loadAndIndexDocuments() {
        // 查询所有启用的文档
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDoc::getStatus, 1);
        List<AiKnowledgeDoc> docs = knowledgeDocMapper.selectList(wrapper);

        if (docs.isEmpty()) {
            log.warn("知识库中没有启用的文档");
            return;
        }

        log.info("找到{}篇启用的文档，开始向量化...", docs.size());

        // 文档分割器：每段500字符，重叠50字符
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);

        for (AiKnowledgeDoc doc : docs) {
            try {
                // 转换为LangChain4j的Document对象
                Document document = Document.from(doc.getContent());

                // 分割文档
                List<TextSegment> segments = splitter.split(document);

                // 向量化并存储
                List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                embeddingStore.addAll(embeddings, segments);

                log.info("文档 [{}] 已向量化，共{}个片段", doc.getDocName(), segments.size());
            } catch (Exception e) {
                log.error("文档 [{}] 向量化失败", doc.getDocName(), e);
            }
        }
    }

    @Override
    public List<String> searchRelevantKnowledge(String query, int maxResults) {
        if (StrUtil.isBlank(query)) {
            return List.of();
        }

        try {
            // 向量化查询
            Embedding queryEmbedding = embeddingModel.embed(query).content();

            // 检索相似片段（相似度阈值0.6）
            List<EmbeddingMatch<TextSegment>> matches =
                    embeddingStore.findRelevant(queryEmbedding, maxResults, 0.6);

            // 提取文本内容
            return matches.stream()
                    .map(match -> match.embedded().text())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("知识检索失败", e);
            return List.of();
        }
    }

    @Override
    public Boolean addDocument(AiKnowledgeDoc doc) {
        try {
            // 保存到数据库
            int result = knowledgeDocMapper.insert(doc);

            // 如果启用状态，立即向量化
            if (doc.getStatus() == 1) {
                Document document = Document.from(doc.getContent());
                DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
                List<TextSegment> segments = splitter.split(document);
                List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
                embeddingStore.addAll(embeddings, segments);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("添加文档失败", e);
            return false;
        }
    }

    @Override
    public void rebuildIndex() {
        log.info("开始重建索引...");
        // 清空现有索引（内存版本直接重新加载即可）
        loadAndIndexDocuments();
        log.info("索引重建完成");
    }
}

