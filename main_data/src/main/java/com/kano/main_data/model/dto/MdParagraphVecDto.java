package com.kano.main_data.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MdParagraphVecDto {
    private String paragraphId;
    private String headingId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
