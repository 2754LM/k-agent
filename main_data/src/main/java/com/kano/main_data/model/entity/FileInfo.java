package com.kano.main_data.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@TableName(autoResultMap = true)
@AllArgsConstructor
public class FileInfo {
    private String id;
    private String name;
    private String type;
    private Long size;
    private String md5;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
