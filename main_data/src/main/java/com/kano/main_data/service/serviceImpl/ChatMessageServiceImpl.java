package com.kano.main_data.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kano.main_data.event.ChatEvent;
import com.kano.main_data.model.converter.ChatMessageConverter;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.ChatMessage;
import com.kano.main_data.model.mapper.ChatMessageMapper;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import com.kano.main_data.model.response.CreateChatMessageResponse;
import com.kano.main_data.service.ChatMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;


@Slf4j
@Service
public class ChatMessageServiceImpl
        extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private ChatMessageConverter chatMessageConverter;

    @Autowired
    private ChatMessageMapper chatMessageMapper;


    @Override
    public CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request) {
        ChatMessageDto chatMessageDto = saveChatMessage(chatMessageConverter.toDto(request));
        publisher.publishEvent(new ChatEvent(request.getAgentId(), chatMessageDto.getChatSessionId(), chatMessageDto.getContent()));
        return new CreateChatMessageResponse();
    }

    @Override
    public ChatMessageDto saveChatMessage(ChatMessageDto dto) {
        ChatMessage entity = chatMessageConverter.toEntity(dto);
        int count = chatMessageMapper.insert(entity);
        if (count != 1) {
            log.error("Failed to save chat message: {}", entity);
        }
        return chatMessageConverter.toDto(entity);
    }

    @Override
    public List<ChatMessageDto> getChatMessagesBySessionId(String sessionId) {
        List<ChatMessage> chatMessages = this.lambdaQuery()
                .eq(ChatMessage::getChatSessionId, sessionId)
                .orderByAsc(ChatMessage::getCreatedAt).list();
        return chatMessages.stream().map(chatMessageConverter::toDto).toList();
    }

    //todo
    @Override
    public List<ChatMessageDto> getChatMessagesBySessionIdRecently(String sessionId) {
        return List.of();
    }
}
