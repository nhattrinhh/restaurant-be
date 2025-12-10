package com.web.web.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.web.web.Service.R2Service;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final R2Service r2Service;

    /**
     * Lấy presigned URL để upload file và URL công khai sau khi upload
     * 
     * @param filename Tên file sẽ upload
     * @return Map chứa presignedUrl và publicUrl
     */
    @GetMapping("/presign")
    public Map<String, String> getPresignedUrl(@RequestParam String filename) {
        String presignedUrl = r2Service.generatePresignedUrl(filename);
        String publicUrl = r2Service.getPublicUrl(filename);

        Map<String, String> response = new HashMap<>();
        response.put("presignedUrl", presignedUrl);
        response.put("publicUrl", publicUrl);
        return response;
    }

    /**
     * Upload file lên R2 từ server (tránh CORS issues)
     * 
     * @param file     File cần upload
     * @param filename Tên file trên R2
     * @return Map chứa publicUrl
     */
    @PostMapping("/file")
    public Map<String, String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {

        String publicUrl = r2Service.uploadFile(file, filename);

        Map<String, String> response = new HashMap<>();
        response.put("publicUrl", publicUrl);
        return response;
    }
}
