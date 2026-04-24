package com.kano.main_data.service;

import com.kano.main_data.model.dto.FileInfoDto;
import com.kano.main_data.model.entity.FileInfo;

public interface MarkDownService {
    void parseMd(FileInfoDto fileInfoDto, String mdContent);
}
