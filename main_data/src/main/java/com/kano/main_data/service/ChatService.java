package com.kano.main_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.ChatMessage;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import com.kano.main_data.model.response.CreateChatMessageResponse;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {
    ChatMessage insertChatMessage(CreateChatMessageRequest request);

    CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request);

    List<ChatMessageDto> getChatMessagesBySessionId(String sessionId);
}
