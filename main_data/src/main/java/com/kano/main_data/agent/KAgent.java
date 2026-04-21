package com.kano.main_data.agent;

import com.kano.main_data.model.common.AgentState;
import com.kano.main_data.service.SseService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;

import java.util.List;

public class KAgent {
    private String chatSessionId;
    private String agentId;
    private ChatMemory chatMemory;
    private ChatClient chatClient;
    private SseService sseService;
    private ChatResponse lastResponse;
    private AgentState agentState;
    private ChatOptions chatOptions;
    private ToolCallingManager toolCallingManager;
    private int MAX_LOOP = 10;

    KAgent(String agentId, String chatSessionId, String systemPrompt, List<Message> messages, ChatClient chatClient, SseService sseService) {
        this.agentId = agentId;
        this.chatSessionId = chatSessionId;
        this.chatClient = chatClient;
        this.sseService = sseService;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(100)
                .build();
        chatMemory.add(chatSessionId, messages);
        chatMemory.add(chatSessionId, new SystemMessage(systemPrompt));
        agentState = AgentState.IDLE;
        chatOptions = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();
        toolCallingManager = ToolCallingManager.builder().build();
    }

    private ChatResponse sendMessage(Prompt prompt) {
        return chatClient.prompt(prompt).stream().chatResponse().
                doOnNext(response -> {
                    String data = response.getResult().getOutput().getText();
                    if (data != null && !data.isEmpty()) {
                        String res = data.replace("\n", "[LF]").replace(" ", "[SP]");
                        sseService.send(chatSessionId, res);
                    }
                }).doFinally(signalType  -> sseService.done(chatSessionId))
                .blockLast();
    }

    private boolean think() {
        String thinkPrompt = "根据当前的系统提示和记忆，判断是否需要执行工具，如果需要，返回工具调用的相关信息，否则返回不需要执行工具。";
        Prompt prompt = Prompt.builder()
                .chatOptions(chatOptions)
                .messages(chatMemory.get(chatSessionId))
                .build();
        ChatResponse chatResponse = sendMessage(prompt);
        //todo 是否有工具调用
        return false;
    }

    private void execute() {

    }

    public void run() {
        for (int i = 0; i < MAX_LOOP; i++) {
            if (think()) {
                execute();
            } else {
                agentState = AgentState.FINISHED;
                break;
            }
        }
    }
}
