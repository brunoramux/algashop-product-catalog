package com.algaworks.algashop.product.catalog.application.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResponseOutput {

    private String remoteFileName;
    private Long contentLength;
    private String contentType;
    private String uploadSignedUrl;
    
}

