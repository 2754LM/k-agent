package com.kano.main_data.registry;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmbeddingModelRegistry {
    @Autowired
    Map<String, EmbeddingModel> embeddingModelMap;

    public EmbeddingModel getEmbeddingModel(String modelName) {
        return embeddingModelMap.get(modelName);
    }
}
