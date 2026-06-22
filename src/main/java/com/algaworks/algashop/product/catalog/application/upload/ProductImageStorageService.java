package com.algaworks.algashop.product.catalog.application.upload;

import java.io.InputStream;

public interface ProductImageStorageService {
    UploadResponseOutput createUploadRequest(String originalFileName, Long contentLength);
    String uploadImage(String originalFileName, InputStream content, long contentLength, String contentType);
    String buildImageUrl(String remoteFileName);
}

