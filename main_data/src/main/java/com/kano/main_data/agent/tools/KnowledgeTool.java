package com.kano.main_data.agent.tools;

import com.kano.main_data.service.RagService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeTool implements com.kano.main_data.agent.tools.Tool {

    @Autowired
    private RagService ragService;

    @Tool(
            name = "knowledge_search",
            description = """
            知识库检索工具。根据用户的问题，从内部知识库中检索最相关的内容片段。
            适用于查询产品文档、技术手册、FAQ、内部规范等结构化或非结构化的文本知识。
            使用方法：传入一个明确的问题（query）以及期望返回的结果数量（topK）。
            返回结果：多个相关文本片段，以换行分隔。每个片段可能包含标题、正文或总结。
            注意：
            - query 应尽量具体、完整，避免模糊或过于宽泛的提问。
            - topK 通常设为 3~10，根据问题复杂度和所需信息量调整。
            - 如果返回结果为空，说明知识库中没有匹配的内容。
            """
    )
    public String knowledgeSearch(
            @ToolParam(description = "需要查询的问题或关键词，应清晰完整，例如：'如何重置管理员密码？' 或 '订单状态有哪些？'")
            String query,
            @ToolParam(description = "需要返回的相关片段数量，建议范围 3~5。值越大结果越多但可能降低精确度。")
            int topK
    ) {
        return String.join("\n", ragService.similaritySearch(query, topK));
    }
}
