package com.hospital.report.ai.config;

import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4J配置类 - 集成千问模型
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.qianwen")
@Data
public class LangChain4JConfig {
    
    private String apiKey;
    private String chatModel = "qwen-max";
    private String embeddingModel = "text-embedding-v2";
    private Integer timeout = 60000;
    private Double temperature = 0.7;
    private Integer maxTokens = 4000;
    private Integer maxRetries = 3;
    
    /**
     * 配置千问聊天模型
     */
    @Bean
    public ChatLanguageModel qwenChatLanguageModel() {
        return QwenChatModel.builder()
            .apiKey(apiKey)
            .modelName(chatModel)
            .temperature(temperature.floatValue())
            .maxTokens(maxTokens)
            .build();
    }
    
    /**
     * 配置千问向量模型
     */
    @Bean
    public EmbeddingModel qwenEmbeddingModel() {
        return QwenEmbeddingModel.builder()
            .apiKey(apiKey)
            .modelName(embeddingModel)
            .build();
    }
}