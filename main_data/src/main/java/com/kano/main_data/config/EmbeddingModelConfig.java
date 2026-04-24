package com.kano.main_data.config;

import com.alibaba.cloud.ai.model.RerankModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmbeddingModelConfig {
    @Bean("bge-m3")
    public EmbeddingModel bgeM3EmbeddingModel(OllamaApi ollamaApi) {
        OllamaEmbeddingOptions options = OllamaEmbeddingOptions.builder()
                .model("bge-m3").build();
        return OllamaEmbeddingModel.builder().ollamaApi(ollamaApi).defaultOptions(options).build();
    }
}
