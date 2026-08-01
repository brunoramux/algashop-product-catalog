package com.algaworks.algashop.product.catalog.application.storage;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.Objects;

@Getter
public class FileReference {

    private String fileName;
    private MediaType mediaType;
    private Long contentLength;
    private Duration expiresIn;
    private boolean allowPublicRead;

    @Builder
    public FileReference(String fileName, MediaType mediaType, Long contentLength, Duration expiresIn, boolean allowPublicRead) {
        Objects.requireNonNull(fileName);
        Objects.requireNonNull(mediaType);
        Objects.requireNonNull(expiresIn);
        if(contentLength <= 0){
            throw new IllegalArgumentException();
        }

        this.fileName = fileName;
        this.mediaType = mediaType;
        this.contentLength = contentLength;
        this.expiresIn = expiresIn;
        this.allowPublicRead = allowPublicRead;
    }
}
