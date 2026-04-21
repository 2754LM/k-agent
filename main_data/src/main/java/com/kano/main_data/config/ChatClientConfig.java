package com.kano.main_data.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean("dashscope-chat")
    public ChatClient dashScopeChatClient(DashScopeChatModel dashScopeChatModel) {
        return ChatClient.create(dashScopeChatModel);
    }

    @Bean("llama3.2")
    public ChatClient qwen3ChatClient(OllamaChatModel ollamaChatModel) {
        OllamaChatOptions options = OllamaChatOptions.builder()
                .model("llama3.2:1b").build();
        return ChatClient.builder(ollamaChatModel).defaultOptions(options).build();
    }
    @Bean("qwen2.5")
    public ChatClient qwen2ChatClient(OllamaChatModel ollamaChatModel) {
        ChatOptions options = ChatOptions.builder()
                .model("qwen2.5:0.5b").build();
        return ChatClient.builder(ollamaChatModel).defaultOptions(options).build();
    }

    @Bean("deepseek")
    public ChatClient deepseekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return ChatClient.create(deepSeekChatModel);
    }
}

