package com.kano.main_data.tool;

import com.kano.main_data.registry.ChatClientRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompressTool {
    @Autowired
    ChatClientRegistry chatClientRegistry;

    // 清洗工具调用结果中的格式噪音
    public ToolResponseMessage compressToolResponse(ToolResponseMessage toolResponseMessage) {
        String system = """
                你是格式降噪工具。你的任务是清洗原始数据中的格式噪音，将其转换为干净的纯文本或Markdown。
                
                目标：剥离HTML标签、多余空白符、转义控制符及无意义的格式冗余，完整保留原文所有实质内容。
                
                铁律：
                - 仅删除无关符号与格式噪音，绝不删减、概括、提炼或修改原文的任何实质内容。
                - 保持原文语义与细节绝对完整，不遗漏任何事实、数值或实体。
                - 直接输出清洗后的纯文本，无任何废话前缀。
                """;

        ChatClient client = chatClientRegistry.getChatClient("deepseek");
        List<ToolResponseMessage.ToolResponse> responses = new ArrayList<>();

        for (ToolResponseMessage.ToolResponse response : toolResponseMessage.getResponses()) {
            String userPrompt = "请清洗以下工具返回数据中的格式噪音：\n\n" + response.responseData();
            ChatResponse chatResponse = client.prompt().system(system).user(userPrompt).call().chatResponse();

            if (chatResponse == null || chatResponse.getResult().getOutput().getText() == null) {
                // 压缩失败，保留原始数据兜底
                responses.add(new ToolResponseMessage.ToolResponse(response.id(), response.name(), response.responseData()));
                continue;
            }
            // 压缩成功，替换为清洗后的文本
            responses.add(new ToolResponseMessage.ToolResponse(response.id(), response.name(), chatResponse.getResult().getOutput().getText()));
        }

        return ToolResponseMessage.builder()
                .responses(responses)
                .metadata(toolResponseMessage.getMetadata())
                .build();
    }
}
