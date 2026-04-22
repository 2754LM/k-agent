package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.model.common.ChatRole;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.registry.ChatClientRegistry;
import com.kano.main_data.service.ChatContextService;
import com.kano.main_data.service.ChatMessageService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatContextServiceImpl implements ChatContextService {
    @Autowired
    ChatMessageService chatMessageService;
    @Autowired
    ChatClientRegistry chatClientRegistry;

    @Override
    public List<Message> loadMemory(String sessionId) {
        //todo 数据库加载
        int startId = 0;
        List<ChatMessageDto> chatMessages = chatMessageService.getChatMessagesBySessionIdRecently(sessionId, startId);
        List<Message> memory = new ArrayList<>();
        for (ChatMessageDto chatMessage : chatMessages) {
            switch (chatMessage.getChatRole()) {
                case USER -> memory.add(new UserMessage(chatMessage.getContent()));
                case ASSISTANT ->
                        memory.add(AssistantMessage.builder().content(chatMessage.getContent()).toolCalls(chatMessage.getMetaData().getToolCalls()).build());
                case SYSTEM -> memory.add(new SystemMessage(chatMessage.getContent()));
                case TOOL ->
                        memory.add(ToolResponseMessage.builder().responses(List.of(chatMessage.getMetaData().getToolResponse())).build());
                default -> throw new IllegalStateException("Unexpected value: " + chatMessage.getChatRole());
            }
        }
        return memory;
    }


    //todo压缩函数
    @Override
    public ChatResponse compressChatMessages(String sessionId) {
        List<Message> messages = loadMemory(sessionId);
        //获取agent
        ChatClient chatClient = chatClientRegistry.getChatClient("deepseek");
        //seesionService获取处理数
        int startId = 0;
        List<ChatMessageDto> chatMessages = chatMessageService.getChatMessagesBySessionIdRecently(sessionId, startId);
        String systemPrompt = """
                你是一个对话压缩助手。请将以下对话内容按结构化格式进行压缩，保留所有关键信息。
                
                压缩规则：
                1. 删除冗余：去掉填充词、重复语句和无意义表达。
                2. 保留核心：确保问题、方案、决策、结论和行动项不被遗漏。
                3. 保持逻辑：压缩后内容应清晰、连贯、易于理解。
                4. 格式统一：按以下结构输出。
                
                输出格式：
                
                【讨论内容】
                简要描述对话的核心主题或问题背景。
                
                【关键点】
                列出讨论中提出的重要观点、方案或信息。
                
                【决策/结论】
                记录最终达成的决策、结论或共识。
                
                【下一步计划】
                列出明确的具体行动项及责任人（如有）。
                
                【备注】（可选）
                其他需要关注的补充信息。
                
                对话内容：""";


        Prompt prompt = Prompt.builder().messages(messages).build();
        ChatResponse chatResponse = chatClient.prompt(prompt).system(systemPrompt).call().chatResponse();
        Usage usage = chatResponse.getMetadata().getUsage();
        String content = chatResponse.getResult().getOutput().getText();
        //更新session
        ChatMessageDto chatMessageDto = ChatMessageDto.builder().agentId("0").chatSessionId(sessionId).content(content).chatRole(ChatRole.SYSTEM).totalTokens(usage.getTotalTokens()).promptTokens(usage.getPromptTokens()).completionTokens(usage.getCompletionTokens()).build();
        chatMessageService.saveChatMessage(chatMessageDto);
        return chatResponse;
    }


}
