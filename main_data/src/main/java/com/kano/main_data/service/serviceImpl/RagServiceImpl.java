package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.model.converter.MdHeadingVecConverter;
import com.kano.main_data.model.converter.MdParagraphVecConverter;
import com.kano.main_data.model.dto.MdHeadingVecDto;
import com.kano.main_data.model.dto.MdParagraphVecDto;
import com.kano.main_data.model.entity.MdHeadingVec;
import com.kano.main_data.model.entity.MdParagraphVec;
import com.kano.main_data.model.mapper.MdHeadingVecMapper;
import com.kano.main_data.model.mapper.MdParagraphVecMapper;
import com.kano.main_data.registry.EmbeddingModelRegistry;
import com.kano.main_data.service.RagService;
import com.kano.main_data.tool.EmbeddingTool;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagServiceImpl implements RagService {
    @Autowired
    EmbeddingTool embeddingTool;
    @Autowired
    MdHeadingVecMapper mdHeadingVecMapper;
    @Autowired
    MdParagraphVecMapper mdParagraphVecMapper;
    @Autowired
    MdHeadingVecConverter mdHeadingVecConverter;
    @Autowired
    MdParagraphVecConverter mdParagraphVecConverter;

    @Override
    public List<String> similaritySearch(String text, int topK) {
        float[] queryVector = embeddingTool.embed(text);
        List<MdHeadingVec> headingVecs = mdHeadingVecMapper.searchByVector(queryVector, topK);
        List<MdParagraphVec> paragraphVecs = mdParagraphVecMapper.searchByVector(queryVector, topK, headingVecs.stream().map(MdHeadingVec::getHeadingId).toList());
        return paragraphVecs.stream().map(MdParagraphVec::getContent).toList();
    }


    @Override
    public void saveMd(List<MdHeadingVecDto> headingVecs, List<MdParagraphVecDto> paragraphVecs) {
        List<MdHeadingVec> mdParagraphVecs = headingVecs.stream().map(mdHeadingVecConverter::toEntity).toList();
        List<MdParagraphVec> mdHeadingVecs = paragraphVecs.stream().map(mdParagraphVecConverter::toEntity).toList();
        mdHeadingVecMapper.insert(mdParagraphVecs);
        mdParagraphVecMapper.insert(mdHeadingVecs);
    }
}
