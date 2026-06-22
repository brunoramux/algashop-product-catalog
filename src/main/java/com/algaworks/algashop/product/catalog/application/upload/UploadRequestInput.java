package com.algaworks.algashop.product.catalog.application.upload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadRequestInput {

    @NotBlank
    private String originalFileName;

    @NotNull
    @Min(1)
    private Long contentLength;
}

