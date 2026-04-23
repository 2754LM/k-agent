package com.kano.main_data.service.serviceImpl;

import com.kano.main_data.model.entity.MdHeadingVec;
import com.kano.main_data.model.entity.MdParagraphVec;
import com.kano.main_data.model.mapper.MdHeadingVecMapper;
import com.kano.main_data.model.mapper.MdParagraphVecMapper;
import com.kano.main_data.registry.EmbeddingModelRegistry;
import com.kano.main_data.service.RagService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagServiceImpl implements RagService {
    @Autowired
    EmbeddingModelRegistry embeddingModelRegistry;
    @Autowired
    MdHeadingVecMapper mdHeadingVecMapper;
    @Autowired
    MdParagraphVecMapper mdParagraphVecMapper;

    @Override
    public float[] embed(String text) {
        EmbeddingModel embeddingModel = embeddingModelRegistry.getEmbeddingModel("bge-m3");
        return embeddingModel.embed(text);
    }

    @Override
    public List<String> similaritySearch(String text, int topK) {
        float[] queryVector = embed(text);
        List<MdHeadingVec> headingVecs = mdHeadingVecMapper.searchByVector(queryVector, topK);
        List<MdParagraphVec> paragraphVecs = mdParagraphVecMapper.searchByVector(queryVector, topK, headingVecs.stream().map(MdHeadingVec::getHeadingId).toList());
        return paragraphVecs.stream().map(MdParagraphVec::getContent).toList();
    }
}
