package com.kano.main_data.service;

import java.util.List;

public interface RagService {
    float[] embed(String text);
    List<String> similaritySearch(String text, int topK);
}
