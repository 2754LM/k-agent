package com.kano.main_data.registry;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class TollRegistry {
    @Autowired
    private List<ToolCallbackProvider> toolCallbackProviders;

    public List<ToolCallback> getAllToolCallbacks() {
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        for(ToolCallbackProvider provider : toolCallbackProviders) {
            Collections.addAll(toolCallbacks, provider.getToolCallbacks());
        }
        return toolCallbacks;
    }
}
