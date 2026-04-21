package com.kano.main_data.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kano.main_data.event.ChatEvent;
import com.kano.main_data.model.converter.ChatMessageConverter;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.ChatMessage;
import com.kano.main_data.model.mapper.ChatMessageMapper;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import com.kano.main_data.model.response.CreateChatMessageResponse;
import com.kano.main_data.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class ChatServiceImpl
        extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatService {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ChatMessageConverter chatMessageConverter;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    public ChatMessage insertChatMessage(CreateChatMessageRequest request) {
        ChatMessageDto chatMessageDto = chatMessageConverter.toDto(request);
        ChatMessage chatMessage = chatMessageConverter.toEntity(chatMessageDto);
        int count = chatMessageMapper.insert(chatMessage);
        if (count != 1) {
            log.error("Failed to insert chat message: {}", chatMessage);
            throw new RuntimeException("Failed to insert chat message");
        }
        return chatMessage;
    }

    @Override
    public CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request) {
        ChatMessage result = insertChatMessage(request);
        publisher.publishEvent(new ChatEvent(request.getAgentId(), result.getChatSessionId(), result.getContent()));
        return new CreateChatMessageResponse();
    }

    @Override
    public List<ChatMessageDto> getChatMessagesBySessionId(String sessionId) {
        List<ChatMessage> chatMessages = this.lambdaQuery()
                .eq(ChatMessage::getChatSessionId, sessionId)
                .orderByDesc(ChatMessage::getCreatedAt).list();
        return chatMessages.stream().map(chatMessageConverter::toDto).toList();
    }
}
