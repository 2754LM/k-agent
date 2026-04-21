package com.kano.main_data.agent;

import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.Agent;
import com.kano.main_data.registry.ChatClientRegistry;
import com.kano.main_data.service.ChatService;
import com.kano.main_data.service.SseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class KAgentFactory {

    @Qualifier("dashscope-chat")
    @Autowired
    private ChatClient chat;

    @Autowired
    private ChatService chatService;

    private List<Message> loadMemory(String sessionId) {
        List<ChatMessageDto> chatMessages = chatService.getChatMessagesBySessionId(sessionId);
        List<Message> memory = new ArrayList<>();
        for (ChatMessageDto chatMessage : chatMessages) {
            switch (chatMessage.getChatRole()) {
                case USER -> memory.add(new UserMessage(chatMessage.getContent()));
                case ASSISTANT -> memory.add(AssistantMessage.builder().content(chatMessage.getContent())
                        .toolCalls(chatMessage.getMetaData().getToolCalls()).build());
                case SYSTEM -> memory.add(new SystemMessage(chatMessage.getContent()));
                case TOOL ->
                        memory.add(ToolResponseMessage.builder().responses(chatMessage.getMetaData().getToolResponses()).build());
                default -> throw new IllegalStateException("Unexpected value: " + chatMessage.getChatRole());
            }
        }
        return memory;
    }

    private Agent LoadAgent(String agentId) {
        //todo db查询
        return new Agent();
    }

    @Autowired
    ChatClientRegistry chatClientRegistry;

    @Autowired
    SseService sseService;

    public KAgent create(String agentId, String sessionId) {
        List<Message> messages = loadMemory(sessionId);
        Agent agent = LoadAgent(agentId);
        //todo 根据agent查询
        ChatClient chatClient = chatClientRegistry.getChatClient("deepseek");
        return new KAgent(agentId, sessionId, "", messages, chatClient, sseService);
    }
}
