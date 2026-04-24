package com.kano.main_data.model.converter;

import com.kano.main_data.model.dto.MdHeadingVecDto;
import com.kano.main_data.model.entity.MdHeadingVec;
import com.kano.main_data.tool.EmbeddingTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MdHeadingVecConverter {
    @Autowired
    EmbeddingTool embeddingTool;
    public MdHeadingVec toEntity(MdHeadingVecDto mdHeadingVecDto) {
        return MdHeadingVec.builder()
                .headingId(mdHeadingVecDto.getHeadingId())
                .content(mdHeadingVecDto.getContent())
                .createdAt(mdHeadingVecDto.getCreatedAt())
                .updatedAt(mdHeadingVecDto.getUpdatedAt())
                .embedding(embeddingTool.embed(mdHeadingVecDto.getContent()))
                .fileId(mdHeadingVecDto.getFileId())
                .build();
    }

    public MdHeadingVecDto toDto(MdHeadingVec mdHeadingVec) {
        return MdHeadingVecDto.builder()
                .headingId(mdHeadingVec.getHeadingId())
                .content(mdHeadingVec.getContent())
                .fileId(mdHeadingVec.getFileId())
                .createdAt(mdHeadingVec.getCreatedAt())
                .updatedAt(mdHeadingVec.getUpdatedAt())
                .build();
    }
}
