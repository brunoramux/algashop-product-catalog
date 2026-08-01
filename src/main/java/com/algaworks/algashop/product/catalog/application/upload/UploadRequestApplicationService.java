package com.algaworks.algashop.product.catalog.application.upload;

import com.algaworks.algashop.product.catalog.application.storage.FileReference;
import com.algaworks.algashop.product.catalog.application.storage.StorageProvider;
import com.algaworks.algashop.product.catalog.application.utility.ImageMediaTypeExtractor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadRequestApplicationService {

    private final StorageProvider storageProvider;

    public UploadResponseOutput requestPreSignedUrl(UploadRequestInput input) {
        MediaType imageMediaType = ImageMediaTypeExtractor.getImageMediaType(input.getOriginalFileName());

        if(!imageMediaType.equals(MediaType.IMAGE_JPEG) || !imageMediaType.equals(MediaType.IMAGE_PNG)) {
            throw new IllegalArgumentException("Invalid image type");
        }

        String extension = FilenameUtils.getExtension(input.getOriginalFileName());

        FileReference fileReference = FileReference.builder()
                .fileName(UUID.randomUUID() + "." + extension)
                .contentLength(input.getContentLength())
                .mediaType(imageMediaType)
                .expiresIn(Duration.ofMinutes(5))
                .build();

        URL url = storageProvider.requestUploadUrl(fileReference);
        OffsetDateTime plus = OffsetDateTime.now().plus(fileReference.getExpiresIn());

        return UploadResponseOutput.builder()
                .remoteFileName(fileReference.getFileName())
                .contentLength(fileReference.getContentLength())
                .contentType(fileReference.getMediaType().toString())
                .uploadSignedUrl(url.toString())
                .expiresAt(plus)
                .build();

    }

}
