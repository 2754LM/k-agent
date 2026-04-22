package com.kano.main_data.agent;

import com.kano.main_data.model.common.AgentState;
import com.kano.main_data.model.common.ChatRole;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.service.ChatContextService;
import com.kano.main_data.service.ChatMessageService;
import com.kano.main_data.service.SseService;
import com.kano.main_data.tool.CompressTool;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

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
    private List<ToolCallback> toolCallbacks;
    private int MAX_LOOP = 10;
    private ChatMessageService chatMessageService;
    private ChatContextService chatContextService;
    private CompressTool compressTool;

    KAgent(String agentId, String chatSessionId, String systemPrompt, List<Message> messages,
           List<ToolCallback> toolCallbacks, ChatClient chatClient, SseService sseService, ChatMessageService chatMessageService,
           ChatContextService chatContextService, CompressTool compressTool) {
        this.agentId = agentId;
        this.chatSessionId = chatSessionId;
        this.chatClient = chatClient;
        this.sseService = sseService;
        this.chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(100)
                .build();
        this.toolCallbacks = toolCallbacks;
        this.chatMessageService = chatMessageService;
        this.chatContextService = chatContextService;
        this.compressTool = compressTool;
        chatMemory.add(chatSessionId, messages);
        agentState = AgentState.IDLE;
        chatOptions = ToolCallingChatOptions.builder().internalToolExecutionEnabled(false).build();
        toolCallingManager = ToolCallingManager.builder().build();
    }

    private ChatResponse sendMessage(Prompt prompt) {
        return sendMessage(prompt, "");
    }

    private ChatResponse sendMessage(Prompt prompt, String systemPrompt) {
        StringBuilder accumulatedText = new StringBuilder();

        ChatResponse finalChunk = chatClient.prompt(prompt)
                .system(systemPrompt)
                .toolCallbacks(toolCallbacks)
                .stream().chatResponse()
                .doOnNext(chunk -> {
                    AssistantMessage output = chunk.getResult().getOutput();
                    String text = output.getText();

                    if (text != null && !text.isEmpty()) {
                        accumulatedText.append(text);
                        sseService.send(chatSessionId, text.replace("\n", "[LF]").replace(" ", "[SP]"));
                    }

                    List<AssistantMessage.ToolCall> toolCalls = output.getToolCalls();
                    if (!toolCalls.isEmpty()) {
                        String toolInfo = toolCalls.stream()
                                .map(call -> "工具调用: " + call.name() + ", 参数: " + call.arguments())
                                .collect(Collectors.joining("\n"));
                        sseService.send(chatSessionId, toolInfo);
                    }
                })
                .blockLast();

        if (finalChunk == null) {
            return null;
        }

        AssistantMessage finalOutput = finalChunk.getResult().getOutput();

        AssistantMessage mergedMessage = AssistantMessage.builder()
                .content(accumulatedText.toString())
                .toolCalls(finalOutput.getToolCalls())
                .media(finalOutput.getMedia())
                .build();

        Generation mergedGeneration = new Generation(mergedMessage, finalChunk.getResult().getMetadata());
        return new ChatResponse(List.of(mergedGeneration), finalChunk.getMetadata());
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
            String toolResponseContent = toolResponseMessage.getResponses()
                    .stream()
                    .map(resp -> "工具" + resp.name() + "的返回结果为：" + resp.responseData())
                    .collect(Collectors.joining("\n"));
            ChatMessageDto chatMessageDto = ChatMessageDto.builder()
                    .chatSessionId(chatSessionId)
                    .content(toolResponseContent)
                    .chatRole(ChatRole.TOOL)
                    .totalTokens(usage.getTotalTokens())
                    .promptTokens(usage.getPromptTokens())
                    .completionTokens(usage.getCompletionTokens())
                    .metaData(ChatMessageDto.MetaData.builder().toolResponse(toolResponseMessage.getResponses()).build())
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
        agentState = AgentState.THINKING;
        String thinkPrompt = """
                你正处于思考决策阶段。请严格按照 [思考] -> [决策] 的流程分析当前状态并输出：
                
                [思考]：根据系统提示和上下文记忆，分析当前要解决的核心问题是什么？我已经掌握了哪些信息？还缺乏哪些关键信息或需要执行什么操作？
                [决策]：基于以上分析，做出二选一决策：
                - 若缺乏信息或需执行操作：必须调用相应工具。不要用自然语言描述意图，直接依赖你的内生能力生成工具调用请求。
                - 若信息已充足无需操作：直接输出最终回答，严禁输出"我不需要执行工具"等废话。
                
                现在，请开始你的思考与决策：
                """;
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
        agentState = AgentState.EXECUTING;
        Prompt prompt = Prompt.builder()
                .messages(chatMemory.get(chatSessionId))
                .chatOptions(chatOptions)
                .build();
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, lastChatResponse);

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) toolExecutionResult
                .conversationHistory()
                .get(toolExecutionResult.conversationHistory().size() - 1);
        ToolResponseMessage compressToolResponse = toolResponseMessage;

        chatMemory.add(chatSessionId, toolExecutionResult.conversationHistory().get(toolExecutionResult.conversationHistory().size() - 2));
        chatMemory.add(chatSessionId, compressToolResponse);

        // 保存工具调用
        saveMessage(compressToolResponse, null);

        if (compressToolResponse.getResponses()
                .stream()
                .anyMatch(resp -> resp.name().equals("terminate"))) {
            agentState = AgentState.FINISHED;
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
