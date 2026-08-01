package com.algaworks.algashop.product.catalog.infrastructure.storage.fake;

import com.algaworks.algashop.product.catalog.application.storage.FileReference;
import com.algaworks.algashop.product.catalog.application.storage.StorageProvider;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.net.URI;

public class StorageProviderFakeImpl implements StorageProvider {

    @Override
    @SneakyThrows
    public java.net.URL requestUploadUrl(FileReference fileReference) {
        return URI.create("https://fake-storage-provider.com/upload/" + fileReference.getFileName()).toURL();
    }

    @Override
    public void deleteFile(String remoteFileName) {

    }

    @Override
    public boolean fileExists(String remoteFileName) {
        return false;
    }
}
