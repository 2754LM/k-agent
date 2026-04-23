package com.kano.main_data.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kano.main_data.model.entity.MdHeadingVec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MdHeadingVecMapper extends BaseMapper<MdHeadingVec> {

    List<MdHeadingVec> searchByVector(@Param("queryVector") float[] queryVectorStr,@Param("topK") int topK);

}
