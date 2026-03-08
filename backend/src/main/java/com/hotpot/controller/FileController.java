package com.hotpot.controller;

import com.hotpot.common.BusinessException;
import com.hotpot.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) throw new BusinessException("文件不能为空");

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        try {
            file.transferTo(new File(dir, filename));
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        return Result.success("/uploads/" + filename);
    }
}
