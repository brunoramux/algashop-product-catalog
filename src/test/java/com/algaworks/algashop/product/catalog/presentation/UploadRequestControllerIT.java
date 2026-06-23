package com.algaworks.algashop.product.catalog.presentation;

import com.algaworks.algashop.product.catalog.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UploadRequestController Integration Tests")
class UploadRequestControllerIT extends IntegrationTestBase {

    @BeforeEach
    void setup() {
        mockStorageService();
    }

    @Test
    @DisplayName("POST /api/v1/upload-requests should return upload response")
    void shouldCreateUploadRequest() throws Exception {
        String body = """
                {"originalFileName": "photo.jpg", "contentLength": 102400}
                """;

        mockMvc.perform(post("/api/v1/upload-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remoteFileName").value("uuid.jpg"))
                .andExpect(jsonPath("$.uploadSignedUrl").value("http://s3/signed-url"))
                .andExpect(jsonPath("$.contentType").value("image/jpeg"))
                .andExpect(jsonPath("$.contentLength").value(1024));
    }

    @Test
    @DisplayName("POST /api/v1/upload-requests should return 400 when originalFileName is blank")
    void shouldReturn400WhenOriginalFileNameIsBlank() throws Exception {
        String body = """
                {"originalFileName": "", "contentLength": 1024}
                """;

        mockMvc.perform(post("/api/v1/upload-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid fields"));
    }

    @Test
    @DisplayName("POST /api/v1/upload-requests should return 400 when contentLength is null")
    void shouldReturn400WhenContentLengthIsNull() throws Exception {
        String body = """
                {"originalFileName": "photo.jpg"}
                """;

        mockMvc.perform(post("/api/v1/upload-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/upload-requests should return 400 when contentLength is zero")
    void shouldReturn400WhenContentLengthIsZero() throws Exception {
        String body = """
                {"originalFileName": "photo.jpg", "contentLength": 0}
                """;

        mockMvc.perform(post("/api/v1/upload-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}

