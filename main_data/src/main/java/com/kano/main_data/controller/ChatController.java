package com.kano.main_data.controller;

import com.kano.main_data.model.common.ApiResult;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import com.kano.main_data.model.response.CreateChatMessageResponse;
import com.kano.main_data.service.ChatMessageService;
import com.kano.main_data.service.serviceImpl.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class ChatController {

    @Autowired
    ChatMessageService chatMessageService;

    @Autowired
    TokenService tokenService;

    @PostMapping(value = "/chat")
    public ApiResult<CreateChatMessageResponse> createChatMessage(@RequestBody CreateChatMessageRequest request) {
        return ApiResult.success(chatMessageService.createChatMessage(request));

    }
}
