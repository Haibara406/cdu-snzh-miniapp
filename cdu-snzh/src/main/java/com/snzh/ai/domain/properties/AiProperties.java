package com.snzh.ai.domain.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author haibara
 * @description AI配置属性
 * @since 2025/10/5
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

    /**
     * LLM模型配置
     */
    private LlmConfig llm = new LlmConfig();

    /**
     * 会话配置
     */
    private ChatConfig chat = new ChatConfig();

    @Data
    public static class LlmConfig {
        /**
         * 阿里云百炼API Key
         */
        private String apiKey;

        /**
         * 模型名称（如：qwen-plus, qwen-max）
         */
        private String modelName = "qwen-plus";

        /**
         * 温度参数（0-1），越高越随机
         */
        private Float temperature = 0.7F;

        /**
         * 最大Token数
         */
        private Integer maxTokens = 2000;

    }

    @Data
    public static class ChatConfig {
        /**
         * 最大保留历史消息数
         */
        private Integer maxHistory = 20;

        /**
         * 会话过期时间（天）
         */
        private Integer sessionTimeout = 7;

        /**
         * 是否启用流式响应
         */
        private Boolean enableStreaming = false;
    }
}

