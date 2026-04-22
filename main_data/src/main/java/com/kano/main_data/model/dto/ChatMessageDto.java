package com.kano.main_data.model.dto;

import com.kano.main_data.model.common.ChatRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.time.LocalDateTime;
import java.util.List;

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
    private int tokenCount;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetaData {
        List<AssistantMessage.ToolCall> toolCalls;
        ToolResponseMessage.ToolResponse toolResponse;
    }
}

