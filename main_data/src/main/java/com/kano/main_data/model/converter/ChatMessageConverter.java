package com.kano.main_data.model.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kano.main_data.model.common.ChatRole;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.ChatMessage;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ChatMessageConverter {
    @Autowired
    private ObjectMapper objectMapper;
    public ChatMessageDto toDto(CreateChatMessageRequest request) {
        return ChatMessageDto.builder()
                .agentId(request.getAgentId())
                .chatSessionId(request.getChatSessionId())
                .content(request.getContent())
                .chatRole(request.getChatRole())
                .metaData(request.getMetaData())
                .build();
    }

    public ChatMessageDto toDto(ChatMessage chatMessage) {
        try {
            return ChatMessageDto.builder()
                    .id(chatMessage.getId())
                    .chatSessionId(chatMessage.getChatSessionId())
                    .content(chatMessage.getContent())
                    .chatRole(ChatRole.fromValue(chatMessage.getRole()))
                    .metaData(objectMapper.readValue(chatMessage.getMetadata(), ChatMessageDto.MetaData.class))
                    .createdAt(chatMessage.getCreatedAt())
                    .updatedAt(chatMessage.getUpdatedAt())
                    .tokenCount(chatMessage.getTokenCount())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatMessage toEntity(ChatMessageDto dto) {
        try {
            return ChatMessage.builder()
                    .chatSessionId(dto.getChatSessionId())
                    .content(dto.getContent())
                    .role(dto.getChatRole().getValue())
                    .metadata(objectMapper.writeValueAsString(dto.getMetaData()))
                    .createdAt(dto.getCreatedAt())
                    .tokenCount(dto.getTokenCount())
                    .updatedAt(dto.getUpdatedAt())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ChatMessage toEntity(CreateChatMessageRequest dto) {
        try {
            return ChatMessage.builder()
                    .chatSessionId(dto.getChatSessionId())
                    .content(dto.getContent())
                    .role(dto.getChatRole().getValue())
                    .metadata(objectMapper.writeValueAsString(dto.getMetaData()))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
