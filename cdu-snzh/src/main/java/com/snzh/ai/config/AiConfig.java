package com.snzh.ai.config;

import com.snzh.ai.domain.properties.AiProperties;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author haibara
 * @description AI配置类
 * @since 2025/10/5
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AiConfig {

    private final AiProperties aiProperties;

    /**
     * 配置通义千问聊天模型（同步阻塞版）
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("初始化通义千问聊天模型（同步），模型名称：{}", aiProperties.getLlm().getModelName());
        
        return QwenChatModel.builder()
                .apiKey(aiProperties.getLlm().getApiKey())
                .modelName(aiProperties.getLlm().getModelName())
                .temperature(aiProperties.getLlm().getTemperature())
                .maxTokens(aiProperties.getLlm().getMaxTokens())
                .build();
    }

    /**
     * 配置通义千问流式聊天模型
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        log.info("初始化通义千问流式聊天模型，模型名称：{}", aiProperties.getLlm().getModelName());
        
        return QwenStreamingChatModel.builder()
                .apiKey(aiProperties.getLlm().getApiKey())
                .modelName(aiProperties.getLlm().getModelName())
                .temperature(aiProperties.getLlm().getTemperature())
                .maxTokens(aiProperties.getLlm().getMaxTokens())
                .build();
    }

    /**
     * 配置嵌入模型（用于向量化）
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        log.info("初始化嵌入模型（本地）");
        return new AllMiniLmL6V2EmbeddingModel();
    }

    /**
     * 配置向量存储（内存版本）
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("初始化向量存储（内存版本）");
        return new InMemoryEmbeddingStore<>();
    }
}

