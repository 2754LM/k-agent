package com.kano.main_data.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kano.main_data.model.entity.MdParagraphVec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MdParagraphVecMapper extends BaseMapper<MdParagraphVec> {

    List<MdParagraphVec> searchByVector(@Param("queryVector") float[] queryVector,
                                        @Param("topK") int topK,
                                        @Param("headingIds") List<String> headingIds);
}
