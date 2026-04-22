package com.kano.main_data.model.dto;

import com.kano.main_data.model.common.ChatRole;
import com.kano.main_data.model.common.MetaData;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageDto {
    private String id;
    private String agentId;
    private String chatSessionId;
    private String content;
    private ChatRole chatRole;
    private MetaData metaData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

