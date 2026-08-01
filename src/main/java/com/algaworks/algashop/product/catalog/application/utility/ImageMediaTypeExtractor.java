package com.algaworks.algashop.product.catalog.application.utility;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;

public class ImageMediaTypeExtractor {

    public static MediaType getImageMediaType(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        if (extension.equalsIgnoreCase("jpg")) {
            extension = "jpeg";
        }

        return MediaType.valueOf("image/" + extension.toLowerCase());
    }

}
