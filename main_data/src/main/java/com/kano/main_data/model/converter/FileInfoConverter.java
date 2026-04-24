package com.kano.main_data.model.converter;

import com.kano.main_data.model.dto.FileInfoDto;
import com.kano.main_data.model.entity.FileInfo;
import org.springframework.stereotype.Component;

@Component
public class FileInfoConverter {
    public FileInfoDto toDto(FileInfo fileInfo) {
        if (fileInfo == null) {
            return null;
        }
        return FileInfoDto.builder()
                .id(fileInfo.getId())
                .name(fileInfo.getName())
                .type(fileInfo.getType())
                .size(fileInfo.getSize())
                .md5(fileInfo.getMd5())
                .createdAt(fileInfo.getCreatedAt())
                .updatedAt(fileInfo.getUpdatedAt())
                .build();
    }

    public FileInfo toEntity(FileInfoDto fileInfoDto) {
        if (fileInfoDto == null) {
            return null;
        }
        return FileInfo.builder()
                .id(fileInfoDto.getId())
                .name(fileInfoDto.getName())
                .type(fileInfoDto.getType())
                .size(fileInfoDto.getSize())
                .md5(fileInfoDto.getMd5())
                .createdAt(fileInfoDto.getCreatedAt())
                .updatedAt(fileInfoDto.getUpdatedAt())
                .build();
    }
}
