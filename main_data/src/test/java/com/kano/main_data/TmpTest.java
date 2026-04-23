package com.kano.main_data;

import com.kano.main_data.registry.EmbeddingModelRegistry;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class TmpTest {
    @Autowired
    private List<ToolCallbackProvider> toolCallbackProviders;
    @Autowired
    EmbeddingModelRegistry embeddingModelRegistry;
    @Test
    public void testToolCallbackProvider() {
        EmbeddingModel embeddingModel = embeddingModelRegistry.getEmbeddingModel("bge-m3");
        float[] helloWorlds = embeddingModel.embed("hello world");
        System.out.println(Arrays.toString(helloWorlds));
    }

}
