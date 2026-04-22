package com.kano.main_data.agent;

import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.Agent;
import com.kano.main_data.model.entity.ChatMessage;
import com.kano.main_data.registry.ChatClientRegistry;
import com.kano.main_data.service.ChatContextService;
import com.kano.main_data.service.ChatMessageService;
import com.kano.main_data.service.SseService;
import com.kano.main_data.service.ToolService;
import com.kano.main_data.agent.tools.Tool;
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
    private ChatMessageService ChatMessageService;
    @Autowired
    private ChatContextService chatContextService;
    @Autowired
    private ToolService toolService;

    private Agent loadAgent(String agentId) {
        //todo db查询
        return new Agent();
    }

    //todo查询agent对应的工具列表
    private List<Tool> loadTools() {
        return toolService.getAllTools();
    }

    @Autowired
    ChatClientRegistry chatClientRegistry;

    @Autowired
    SseService sseService;

    public KAgent create(String agentId, String sessionId) {
        List<Message> messages = chatContextService.loadMemory(sessionId);
        Agent agent = loadAgent(agentId);
        //todo 根据agent查询
        ChatClient chatClient = chatClientRegistry.getChatClient("deepseek");
        List<Tool> tools = loadTools();
        return new KAgent(agentId, sessionId, "", messages, tools, chatClient, sseService, ChatMessageService, chatContextService);
    }
}
