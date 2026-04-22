package com.kano.main_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kano.main_data.model.dto.ChatMessageDto;
import com.kano.main_data.model.entity.ChatMessage;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import com.kano.main_data.model.response.CreateChatMessageResponse;

import java.util.List;

public interface ChatMessageService extends IService<ChatMessage> {

    CreateChatMessageResponse createChatMessage(CreateChatMessageRequest request);

    ChatMessageDto saveChatMessage(ChatMessageDto chatMessageDto);

    List<ChatMessageDto> getChatMessagesBySessionId(String sessionId);

    List<ChatMessageDto> getChatMessagesBySessionIdRecently(String sessionId);

}
