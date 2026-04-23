package com.kano.main_data.tool;

import com.kano.main_data.registry.EmbeddingModelRegistry;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmbeddingTool {
    @Autowired
    EmbeddingModelRegistry embeddingModelRegistry;
    public float[] embed(String text) {
        EmbeddingModel embeddingModel = embeddingModelRegistry.getEmbeddingModel("bge-m3");
        return embeddingModel.embed(text);
    }
}
