package com.kano.main_data.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseService {
    SseEmitter connect(String chatSessionId);

    void send(String chatSessionId, String message);

    void done(String chatSessionId);

}
