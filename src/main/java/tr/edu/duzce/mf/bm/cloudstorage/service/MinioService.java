package tr.edu.duzce.mf.bm.cloudstorage.service;

import io.minio.*;
import io.minio.errors.*;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket}")
    public String bucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() throws Exception {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    public String uploadFile(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains("/")) {
            originalFilename = originalFilename.substring(originalFilename.lastIndexOf('/') + 1);
        }
        String storedName = UUID.randomUUID() + "_" + originalFilename;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(storedName)
                        .stream(file.getInputStream(), file.getSize(), -1L)
                        .contentType(file.getContentType())
                        .build());
        return storedName;
    }

    public InputStream downloadFile(String storedName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(storedName).build());
    }

    public void deleteFile(String storedName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucket).object(storedName).build());
    }

    public void copyFile(String sourceName, String destName) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucket)
                        .object(destName)
                        .source(CopySource.builder().bucket(bucket).object(sourceName).build())
                        .build());
    }
}
