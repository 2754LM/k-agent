package com.kano.main_data.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MdHeadingVecDto {
    private String headingId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String fileId;
}
