package com.moneybuddy.moneylog.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Component
public class ClovaOcrClient {

    @Value("${clova.ocr.url}")
    private String ocrUrl;

    @Value("${clova.ocr.secret}")
    private String ocrSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String callOcrApi(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> imageData = Map.of(
                    "format", "jpg",
                    "name", "receipt",
                    "data", base64Image
            );

            Map<String, Object> requestPayload = Map.of(
                    "images", List.of(imageData),
                    "version", "V2",
                    "requestId", UUID.randomUUID().toString(),
                    "timestamp", System.currentTimeMillis()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", ocrSecret);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestPayload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    ocrUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            return response.getBody();
        }
        catch (IOException e) {
            throw new RuntimeException("OCR API 호출 중 오류 발생", e);
        }
    }
}
