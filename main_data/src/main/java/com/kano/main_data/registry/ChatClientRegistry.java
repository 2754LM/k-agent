package com.kano.main_data.registry;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatClientRegistry {
    @Autowired
    Map<String, ChatClient> clients;

    public ChatClient getChatClient(String name) {
        return clients.get(name);
    }
}
