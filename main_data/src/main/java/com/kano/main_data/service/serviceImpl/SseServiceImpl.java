package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.context.SessionContext;
import com.kano.main_data.service.SseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseServiceImpl implements SseService {
    Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter connect(String chatSessionId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        emitters.put(chatSessionId, emitter);
        emitter.onCompletion(() -> emitters.remove(chatSessionId));
        emitter.onTimeout(() -> emitters.remove(chatSessionId));
        emitter.onError((error) -> emitters.remove(chatSessionId));
        log.info("Client connected: {}", chatSessionId);
        try {
            emitter.send(SseEmitter.event().
                    name("init")
                    .data("connected"));
            log.info("Sent init event to client: {}", chatSessionId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return emitter;
    }

    @Override
    public void send(String chatSessionId, String message) {
        SseEmitter emitter = emitters.get(chatSessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("message")
                        .data(message));
                log.info("Sent message to client {}: {}", chatSessionId, message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.warn("No client found for chatSessionId: {}", chatSessionId);
        }
    }


}
