package com.kano.main_data.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kano.main_data.handler.JsonbTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@TableName(autoResultMap = true)
@AllArgsConstructor
public class ChatMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    private String chatSessionId;
    private String content;
    private String role;
    private int tokenCount;
    //json
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
