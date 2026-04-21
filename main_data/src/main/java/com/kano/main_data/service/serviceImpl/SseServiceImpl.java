package com.kano.main_data.service.serviceImpl;

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

        // 注册 + 清理回调
        emitters.put(chatSessionId, emitter);
        emitter.onCompletion(() -> emitters.remove(chatSessionId));
        emitter.onTimeout(() -> {emitters.remove(chatSessionId);});
        emitter.onError((e) -> {emitters.remove(chatSessionId);});

        // 发送连接成功事件
        try {
            emitter.send(SseEmitter.event().name("start").data("connected"));
        } catch (IOException e) {
            emitters.remove(chatSessionId);
        }

        return emitter;
    }

    @Override
    public void send(String chatSessionId, String message) {
        SseEmitter emitter = emitters.get(chatSessionId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("message").data(message));
            } catch (IOException e) {
                log.error("Failed to send message: {}", chatSessionId, e);
                throw new RuntimeException(e);
            }
        } else {
            log.warn("Emitter not found: {}", chatSessionId);
        }
    }

    @Override
    public void done(String chatMessageId) {
        SseEmitter emitter = emitters.get(chatMessageId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("done").data("done"));
                emitter.complete();
            } catch (IOException e) {
                log.error("Failed to send done event: {}", chatMessageId, e);
            }
        }
    }


}
