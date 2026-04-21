package com.kano.main_data.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kano.main_data.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

}
