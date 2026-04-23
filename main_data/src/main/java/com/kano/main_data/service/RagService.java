package com.kano.main_data.service;

import com.kano.main_data.model.dto.MdHeadingVecDto;
import com.kano.main_data.model.dto.MdParagraphVecDto;

import java.util.List;

public interface RagService {

    List<String> similaritySearch(String text, int topK);

    void saveMd(List<MdHeadingVecDto> headingVecs, List<MdParagraphVecDto> paragraphVecs);
}
