package com.kano.main_data.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class UserController {
    @GetMapping("/login")
    public String login(HttpServletResponse response) {
        Cookie cookie = new Cookie("Session-Id", "123456");
        cookie.setPath("/");
        cookie.setMaxAge(30 * 60);
        response.addCookie(cookie);
        log.info("User logged in");
        return "test1";
    }
}
