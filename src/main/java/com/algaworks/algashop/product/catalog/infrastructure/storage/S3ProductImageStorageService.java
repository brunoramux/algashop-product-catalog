package com.algaworks.algashop.product.catalog.infrastructure.storage;

import com.algaworks.algashop.product.catalog.application.upload.ProductImageStorageService;
import com.algaworks.algashop.product.catalog.application.upload.UploadResponseOutput;
import com.algaworks.algashop.product.catalog.domain.model.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class S3ProductImageStorageService implements ProductImageStorageService {

    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${algashop.storage.s3.bucket}")
    private String bucket;

    @Value("${algashop.storage.images.base-url}")
    private String imagesBaseUrl;

    @Value("${algashop.storage.s3.presigned-url-expiration-minutes:15}")
    private int presignedUrlExpirationMinutes;

    @Override
    public UploadResponseOutput createUploadRequest(String originalFileName, Long contentLength) {
        String extension = extractExtension(originalFileName);
        String contentType = resolveContentType(extension);
        String remoteFileName = IdGenerator.generateTimeBasedUUID() + "." + extension;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(remoteFileName)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignedUrlExpirationMinutes))
                        .putObjectRequest(objectRequest)
                        .build()
        );

        return UploadResponseOutput.builder()
                .remoteFileName(remoteFileName)
                .contentLength(contentLength)
                .contentType(contentType)
                .uploadSignedUrl(presignedRequest.url().toString())
                .build();
    }

    @Override
    public String uploadImage(String originalFileName, InputStream content, long contentLength, String contentType) {
        String extension = extractExtension(originalFileName);
        String resolvedContentType = (contentType != null && !contentType.isBlank())
                ? contentType
                : resolveContentType(extension);
        String remoteFileName = IdGenerator.generateTimeBasedUUID() + "." + extension;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(remoteFileName)
                .contentType(resolvedContentType)
                .contentLength(contentLength)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(content, contentLength));

        return remoteFileName;
    }

    @Override
    public String buildImageUrl(String remoteFileName) {
        return imagesBaseUrl.endsWith("/")
                ? imagesBaseUrl + remoteFileName
                : imagesBaseUrl + "/" + remoteFileName;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "jpg";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private String resolveContentType(String extension) {
        return ALLOWED_EXTENSIONS.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }
}

