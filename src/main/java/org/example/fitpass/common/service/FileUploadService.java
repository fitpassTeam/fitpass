package org.example.fitpass.common.service;

import lombok.RequiredArgsConstructor;
import org.example.fitpass.common.error.FileUploadFailException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final String uploadDir = "/uploads/user/";

    public String upload(MultipartFile file) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filepath = Paths.get(uploadDir, filename);

            Files.createDirectories(filepath.getParent());
            Files.write(filepath, file.getBytes());

            return "/images/user/" + filename; // URL 패턴에 맞게 반환
        } catch (IOException e) {
            throw new FileUploadFailException("이미지 업로드 실패");
        }
    }
}