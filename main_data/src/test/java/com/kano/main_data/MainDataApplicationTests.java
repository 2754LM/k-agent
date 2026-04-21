package com.kano.main_data;

import org.springframework.ai.chat.client.ChatClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MainDataApplicationTests {

    // ✅ 直接注入 ChatClient，无需手动构建
    @Autowired
    @Qualifier("qwen")
    private ChatClient chatClient;

    @Test
    void contextLoads() {
        ChatClientResponse result = chatClient.prompt()
                .user("你好")
                .options(ChatOptions.builder()
                        .temperature(0.0)
                        .maxTokens(1000)
                        .build())
                .call()
                        .chatClientResponse();


        System.out.println(result);
    }
}
