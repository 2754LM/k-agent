package com.kano.main_data;

import com.kano.main_data.registry.EmbeddingModelRegistry;
import com.kano.main_data.service.MarkDownService;
import com.kano.main_data.service.RagService;
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
    @Autowired
    MarkDownService markDownService;
    @Autowired
    RagService ragService;
    @Test
    public void testToolCallbackProvider() {
        for(var i : ragService.similaritySearch("ai的功能特色", 100)) {
            System.out.println(i);
        }
    }

}
