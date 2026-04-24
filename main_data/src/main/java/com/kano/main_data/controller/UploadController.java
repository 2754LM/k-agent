package com.kano.main_data.controller;

import com.kano.main_data.model.common.ApiResult;
import com.kano.main_data.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {
    @Autowired
    UploadService uploadService;
    @PostMapping("/upload")
    public ApiResult<Boolean> uploadFile(@RequestParam("file") MultipartFile file) {
        return ApiResult.success(uploadService.uploadFile(file));
    }
}
