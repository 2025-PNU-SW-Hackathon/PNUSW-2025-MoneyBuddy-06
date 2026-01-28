package com.moneybuddy.moneylog.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@Component
public class ClovaReceiptOcrClient {

    @Value("${clova.ocr.url}")
    private String ocrUrl;

    @Value("${clova.ocr.secret}")
    private String ocrSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    public JsonNode requestReceiptOcr(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);

            Map<String, Object> image = Map.of(
                    "format", guessFormat(file.getOriginalFilename()),
                    "name", Optional.ofNullable(file.getOriginalFilename()).orElse("receipt"),
                    "data", base64
            );
            Map<String, Object> payload = Map.of(
                    "version", "V2",
                    "requestId", UUID.randomUUID().toString(),
                    "timestamp", System.currentTimeMillis(),
                    "images", List.of(image)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-OCR-SECRET", ocrSecret);

            HttpEntity<String> entity = new HttpEntity<>(om.writeValueAsString(payload), headers);
            ResponseEntity<String> res = restTemplate.postForEntity(ocrUrl, entity, String.class);

            if (!res.getStatusCode().is2xxSuccessful())
                throw new IllegalStateException("CLOVA OCR 오류: HTTP " + res.getStatusCode().value());

            return om.readTree(res.getBody());
        } catch (IOException e) {
            throw new RuntimeException("이미지/요청 처리 실패", e);
        }
    }

    private String guessFormat(String filename) {
        if (filename == null) return "jpg";
        String f = filename.toLowerCase(Locale.ROOT);
        if (f.endsWith(".png")) return "png";
        if (f.endsWith(".jpeg")) return "jpeg";
        if (f.endsWith(".tif")) return "tif";
        if (f.endsWith(".tiff")) return "tiff";
        return "jpg";
    }
}
