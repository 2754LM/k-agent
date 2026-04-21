package com.kano.main_data.interceptor;

import com.kano.main_data.context.SessionContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class SessionContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String sessionId = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie :cookies) {
            if ("Session-Id".equals(cookie.getName())) {
                sessionId = cookie.getValue();
                break;
            }
        }
        SessionContext.setSessionId(sessionId);
        log.info("SessionContextInterceptor - 设置 SessionId: {}", sessionId);
        return true;
    }
}
