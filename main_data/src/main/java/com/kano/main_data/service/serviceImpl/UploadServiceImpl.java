package com.kano.main_data.service.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kano.main_data.model.converter.FileInfoConverter;
import com.kano.main_data.model.dto.FileInfoDto;
import com.kano.main_data.model.entity.FileInfo;
import com.kano.main_data.model.mapper.FileInfoMapper;
import com.kano.main_data.service.MarkDownService;
import com.kano.main_data.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UploadServiceImpl  extends ServiceImpl<FileInfoMapper, FileInfo> implements UploadService{
    @Autowired
    FileInfoMapper fileInfoMapper;
    @Autowired
    MarkDownService markDownService;
    @Autowired
    FileInfoConverter fileInfoConverter;
    public Boolean uploadFile(MultipartFile file) {
        long size = file.getSize();
        //限制50mb
        if (size > 50 * 1024 * 1024) {
            return false;
        }
        byte[] bytes = null;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String md5 = DigestUtils.md5DigestAsHex(bytes);
        if(lambdaQuery().eq(FileInfo::getMd5, md5).count() > 0) {
            return true;
        }
        FileInfo fileInfo = FileInfo.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .name(file.getOriginalFilename())
                .type("markdown")
                .size(file.getSize())
                .md5(md5)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        fileInfoMapper.insert(fileInfo);
        FileInfoDto fileInfoDto = fileInfoConverter.toDto(fileInfo);
        markDownService.parseMd(fileInfoDto, new String(bytes));
        return true;
    }
}
