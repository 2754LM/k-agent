package com.kano.main_data.model.converter;

import com.kano.main_data.model.dto.MdParagraphVecDto;
import com.kano.main_data.model.entity.MdParagraphVec;
import com.kano.main_data.tool.EmbeddingTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MdParagraphVecConverter {
    @Autowired
    EmbeddingTool embeddingTool;
    public MdParagraphVec toEntity(MdParagraphVecDto mdParagraphVecDto) {
        return MdParagraphVec.builder()
                .paragraphId(mdParagraphVecDto.getParagraphId())
                .headingId(mdParagraphVecDto.getHeadingId())
                .content(mdParagraphVecDto.getContent())
                .createdAt(mdParagraphVecDto.getCreatedAt())
                .updatedAt(mdParagraphVecDto.getUpdatedAt())
                .embedding(embeddingTool.embed(mdParagraphVecDto.getContent()))
                .build();
    }

    public MdParagraphVecDto toDto(MdParagraphVec mdParagraphVec) {
        return MdParagraphVecDto.builder()
                .paragraphId(mdParagraphVec.getParagraphId())
                .headingId(mdParagraphVec.getHeadingId())
                .content(mdParagraphVec.getContent())
                .createdAt(mdParagraphVec.getCreatedAt())
                .updatedAt(mdParagraphVec.getUpdatedAt())
                .build();
    }
}
