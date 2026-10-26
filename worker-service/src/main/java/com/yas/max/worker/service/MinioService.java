package com.yas.max.worker.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    public InputStream downloadFile(String objectName) throws Exception {
        log.info("Завантаження файлу {} з MinIO", objectName);
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    public String getFileContent(String objectName) throws Exception {
        log.info("Читання тексту з файлу {} у MinIO", objectName);
        try (InputStream stream = downloadFile(objectName)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
