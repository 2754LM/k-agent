package com.kano.main_data.config;

import com.kano.main_data.agent.tools.Tool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class LocalToolConfig {
    @Autowired
    private List<Tool> tools;
    @Bean
    public MethodToolCallbackProvider methodToolCallbackProvider() {
        return MethodToolCallbackProvider.builder().toolObjects(tools.toArray()).build();
    }
}
