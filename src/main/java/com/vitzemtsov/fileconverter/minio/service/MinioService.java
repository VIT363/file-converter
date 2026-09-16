package com.vitzemtsov.fileconverter.minio.service;

import com.vitzemtsov.fileconverter.exception.retrayable.TechnicalException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RequiredArgsConstructor
@Service
@Generated
public class MinioService {

    private final MinioClient minioClient;

    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new TechnicalException("Failed to download file from MinIO", e);
        }
    }

    public void uploadFile(String bucketName, String objectName, byte[] content) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType("application/pdf")
                    .build());
        } catch (Exception e) {
            throw new TechnicalException("Failed to upload file to MinIO", e);
        }
    }
}

