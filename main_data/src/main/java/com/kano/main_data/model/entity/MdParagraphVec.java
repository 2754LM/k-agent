package com.kano.main_data.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kano.main_data.handler.JsonbTypeHandler;
import com.kano.main_data.handler.VectorTypeHandler;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(autoResultMap = true)
@Builder
public class MdParagraphVec {
    @TableId
    private String paragraphId;

    private String headingId;

    private String content;

    @TableField(typeHandler = JsonbTypeHandler.class)
    private String metadata;
    @TableField(typeHandler = VectorTypeHandler.class)
    private float[] embedding;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}