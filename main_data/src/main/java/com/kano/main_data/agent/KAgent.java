package com.kano.main_data.agent;

import com.kano.main_data.model.common.AgentState;
import com.kano.main_data.model.common.ChatRole;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.service.ChatContextService;
import com.kano.main_data.service.ChatMessageService;
import com.kano.main_data.service.SseService;
import com.kano.main_data.agent.tools.Tool;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class KAgent {
    private String chatSessionId;
    private String agentId;
    private ChatMemory chatMemory;
    private ChatClient chatClient;
    private SseService sseService;
    private ChatResponse lastChatResponse;
    private AgentState agentState;
    private ChatOptions chatOptions;
    private ToolCallingManager toolCallingManager;
    private List<Tool> tools;
    private int MAX_LOOP = 10;
    private ChatMessageService chatMessageService;
    private ChatContextService chatContextService;

    KAgent(String agentId, String chatSessionId, String systemPrompt, List<Message> messages,
           List<Tool> tools, ChatClient chatClient, SseService sseService, ChatMessageService chatMessageService,
           ChatContextService chatContextService) {
        this.agentId = agentId;
        this.chatSessionId = chatSessionId;
        this.chatClient = chatClient;
        this.sseService = sseService;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(100)
                .build();
        this.tools = tools;
        this.chatMessageService = chatMessageService;
        this.chatContextService = chatContextService;
        chatMemory.add(chatSessionId, messages);
        chatMemory.add(chatSessionId, new SystemMessage(systemPrompt));
        agentState = AgentState.IDLE;
        chatOptions = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();
        toolCallingManager = ToolCallingManager.builder().build();
    }

    private ChatResponse sendMessage(Prompt prompt) {
        return sendMessage(prompt, "");
    }

    private ChatResponse sendMessage(Prompt prompt, String systemPrompt) {
        return chatClient.prompt(prompt)
                .system(systemPrompt)
                .tools(tools.toArray())
                .stream().chatResponse().
                doOnNext(response -> {
                    String text = response.getResult().getOutput().getText();
                    if (text != null && !text.isEmpty()) {
                        String res = text.replace("\n", "[LF]").replace(" ", "[SP]");
                        sseService.send(chatSessionId, res);
                    }
                    List<AssistantMessage.ToolCall> toolCalls = response.getResult().getOutput().getToolCalls();
                    if (!toolCalls.isEmpty()) {
                        String toolCallInfo = toolCalls.stream()
                                .map(call -> "工具调用: " + call.name() + ", 参数: " + call.arguments())
                                .collect(Collectors.joining("\n"));
                        sseService.send(chatSessionId, toolCallInfo);
                    }
                })
                .blockLast();
    }

    private void saveMessage(Message message, Usage usage) {
        if (usage == null) {
            usage = new DefaultUsage(null, null, null);
        }
        if (message instanceof AssistantMessage assistantMessage) {
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .chatSessionId(chatSessionId)
                    .content(assistantMessage.getText())
                    .chatRole(ChatRole.ASSISTANT)
                    .totalTokens(usage.getTotalTokens())
                    .promptTokens(usage.getPromptTokens())
                    .completionTokens(usage.getCompletionTokens())
                    .metaData(ChatMessageDto.MetaData.builder().toolCalls(assistantMessage.getToolCalls()).build())
                    .build();
            chatMessageService.saveChatMessage(chatMessageDto);
        } else if (message instanceof ToolResponseMessage toolResponseMessage) {
            for (ToolResponseMessage.ToolResponse respons : toolResponseMessage.getResponses()) {
                ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                        .chatSessionId(chatSessionId)
                        .content(respons.responseData())
                        .chatRole(ChatRole.TOOL)
                        .totalTokens(usage.getTotalTokens())
                        .promptTokens(usage.getPromptTokens())
                        .completionTokens(usage.getCompletionTokens())
                        .metaData(ChatMessageDto.MetaData.builder().toolResponse(respons).build())
                        .build();
                chatMessageService.saveChatMessage(chatMessageDto);
            }
        } else if (message instanceof SystemMessage systemMessage) {
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .chatSessionId(chatSessionId)
                    .content(systemMessage.getText())
                    .chatRole(ChatRole.SYSTEM)
                    .totalTokens(usage.getTotalTokens())
                    .promptTokens(usage.getPromptTokens())
                    .completionTokens(usage.getCompletionTokens())
                    .build();
            chatMessageService.saveChatMessage(chatMessageDto);
        } else {
            log.error("不支持的 Message 类型: {}, content = {}",
                    message.getClass().getSimpleName(),
                    message.getText()
            );
            throw new IllegalStateException("不支持的 Message 类型");
        }
    }

    private boolean think() {
        String thinkPrompt = "根据当前的系统提示和记忆，判断是否需要执行工具，如果需要，返回工具调用的相关信息，否则返回不需要执行工具。";
        Prompt prompt = Prompt.builder()
                .chatOptions(chatOptions)
                .messages(chatMemory.get(chatSessionId))
                .build();
        lastChatResponse = sendMessage(prompt, thinkPrompt);
        AssistantMessage message = lastChatResponse.getResult().getOutput();
        Usage usage = lastChatResponse.getMetadata().getUsage();
        saveMessage(message, usage);
        return !message.getToolCalls().isEmpty();
    }

    private void execute() {
        Prompt prompt = Prompt.builder()
                .messages(this.chatMemory.get(this.chatSessionId))
                .chatOptions(this.chatOptions)
                .build();
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, this.lastChatResponse);
        this.chatMemory.clear(this.chatSessionId);
        this.chatMemory.add(this.chatSessionId, toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory()
                .get(toolExecutionResult.conversationHistory().size() - 1);

        String collect = toolResponseMessage.getResponses()
                .stream()
                .map(resp -> "工具" + resp.name() + "的返回结果为：" + resp.responseData())
                .collect(Collectors.joining("\n"));
        log.info("工具调用结果：{}", collect);

        // 保存工具调用
        saveMessage(toolResponseMessage, null);

        if (toolResponseMessage.getResponses()
                .stream()
                .anyMatch(resp -> resp.name().equals("terminate"))) {
            this.agentState = AgentState.FINISHED;
            log.info("任务结束");
        }

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
        sseService.done(chatSessionId);
    }
}
