package com.kano.main_data.event.listener;

import com.kano.main_data.agent.KAgent;
import com.kano.main_data.agent.KAgentFactory;
import com.kano.main_data.event.ChatEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ChatEventListener {
    private final KAgentFactory kAgentFactory;

    @Async("chatEventExecutor")
    @EventListener
    public void handle(ChatEvent event) {
        KAgent kAgent = kAgentFactory.create(event.getAgentId(), event.getSessionId());
        kAgent.run();
    }
}
