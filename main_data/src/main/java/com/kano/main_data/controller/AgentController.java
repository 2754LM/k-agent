package com.kano.main_data.controller;

import com.kano.main_data.model.common.ApiResult;
import com.kano.main_data.model.request.CreateAgentRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {
    @PostMapping("/agents")
    public ApiResult<Void> createAgent(@RequestBody CreateAgentRequest request) {
        return ApiResult.success();
    }
}
