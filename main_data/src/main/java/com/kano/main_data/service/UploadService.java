package com.kano.main_data.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kano.main_data.model.entity.FileInfo;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService extends IService<FileInfo> {
    public Boolean uploadFile(MultipartFile file);
}
