package com.kano.main_data.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaData {
    List<AssistantMessage.ToolCall> toolCalls;
    ToolResponseMessage.ToolResponse toolResponse;
    Integer tokenCount;
}
