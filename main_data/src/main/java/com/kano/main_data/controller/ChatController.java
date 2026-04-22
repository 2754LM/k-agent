package com.kano.main_data.controller;

import com.kano.main_data.model.common.ApiResult;
import com.kano.main_data.model.request.CreateChatMessageRequest;
import com.kano.main_data.model.response.CreateChatMessageResponse;
import com.kano.main_data.service.ChatContextService;
import com.kano.main_data.service.ChatMessageService;
import com.kano.main_data.service.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class ChatController {

    @Autowired
    ChatMessageService chatMessageService;
    @Autowired
    ChatContextService chatContextService;
    @Autowired
    TokenService tokenService;

    @PostMapping(value = "/chat")
    public ApiResult<CreateChatMessageResponse> createChatMessage(@RequestBody CreateChatMessageRequest request) {
        return ApiResult.success(chatMessageService.createChatMessage(request));

    }

    @GetMapping(value = "/tokens/count")
    public ApiResult<Integer> countTokens(@RequestParam String text) {
        return ApiResult.success(tokenService.countTokens(text));
    }

    //压缩会话
    @GetMapping(value = "/chat/compress")
    public ApiResult<Void> compressChatMessages(@RequestParam String sessionId) {
        chatContextService.compressChatMessages(sessionId);
        return ApiResult.success();
    }
}
