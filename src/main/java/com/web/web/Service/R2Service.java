package com.web.web.Service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
@RequiredArgsConstructor
public class R2Service {

    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicUrl;

    /**
     * Tạo presigned URL để upload file lên R2
     * 
     * @param filename Tên file sẽ upload
     * @return Presigned URL
     */
    public String generatePresignedUrl(String filename) {
        // Request mô tả file sẽ upload
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(filename)
                .contentType("image/*")
                .build();

        // Tạo presigned URL đúng cách
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);

        return presignedRequest.url().toString();
    }

    /**
     * Upload file lên R2 từ backend (server-side upload - tránh CORS)
     * 
     * @param file     File cần upload
     * @param filename Tên file trên R2
     * @return URL công khai của file
     */
    public String uploadFile(MultipartFile file, String filename) {
        try {
            // Upload file trực tiếp lên R2 từ server
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(
                    file.getInputStream(), file.getSize()));

            // Trả về public URL
            return getPublicUrl(filename);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload file lên R2: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo URL công khai để truy cập file sau khi upload
     * 
     * @param filename Tên file đã upload
     * @return Public URL
     */
    public String getPublicUrl(String filename) {
        // Đảm bảo filename không bắt đầu bằng /
        String cleanFilename = filename.startsWith("/") ? filename.substring(1) : filename;
        return publicUrl + "/" + cleanFilename;
    }
}
