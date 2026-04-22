package com.kano.main_data.service;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;

import java.util.List;

public interface ChatContextService {
    List<Message> loadMemory(String sessionId);

    ChatResponse compressChatMessages(String sessionId);
}

