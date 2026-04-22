package com.kano.main_data.model.request;

import com.kano.main_data.model.common.ChatRole;
import com.kano.main_data.model.dto.ChatMessageDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateChatMessageRequest {
    private String agentId;
    private String chatSessionId;
    private String content;
    private ChatRole chatRole;
    private ChatMessageDto.MetaData metaData;

}
