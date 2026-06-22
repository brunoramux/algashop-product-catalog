package com.algaworks.algashop.product.catalog.application.upload;

public interface ProductImageStorageService {
    UploadResponseOutput createUploadRequest(String originalFileName, Long contentLength);
    String buildImageUrl(String remoteFileName);
}

