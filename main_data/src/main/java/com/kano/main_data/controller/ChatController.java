package com.kano.main_data.controller;

import com.kano.main_data.model.common.ApiResult;
import com.kano.main_data.tools.Tool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.DefaultToolCallingChatOptions;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    @Qualifier("qwen")
    private ChatClient chatClient;

    @Autowired
    ChatMemory chatMemory;

    private String sessionId = "test-session-001";

    private ChatOptions chatOptions;

    @Autowired
    List<Tool> tools;

    TokenCountEstimator tokenCountEstimator = new JTokkitTokenCountEstimator();

    @PostMapping(value = "/chat")
    public ApiResult<ChatResponse> chat(@RequestBody String message) {
        chatOptions = DefaultToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
        Message userMessage = new UserMessage(message);
        chatMemory.add(sessionId, userMessage);
        List<Message> messages = chatMemory.get(sessionId);
        messages.add(new SystemMessage("你是我的人工智能助手，协助我获取信息并回答问题。"));
        Prompt prompt = Prompt.builder().messages(chatMemory.get(sessionId)).build();
        List<ToolCallback> toolCallbacks = new ArrayList<>();
        return ApiResult.success(chatClient.prompt(prompt)
                .options(chatOptions)
                .tools(tools.toArray())
                .call().chatResponse());
    }

    private int tokenCount(String text) {
        return tokenCountEstimator.estimate(text);
    }
}
