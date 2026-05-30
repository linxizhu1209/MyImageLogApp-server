package com.imglog.myimagelogserver.image.controller;

import com.imglog.myimagelogserver.image.storage.LocalStorage;
import com.imglog.myimagelogserver.security.crypto.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * uploads 디렉터리의 이미지를 복호화하여 제공합니다.
 */
@RestController
@RequiredArgsConstructor
public class FileServeController {

    private final LocalStorage localStorage;
    private final EncryptionService encryptionService;

    @GetMapping("/files/{objectKey:.+}")
    public ResponseEntity<byte[]> serve(@PathVariable String objectKey) throws Exception {
        Path file = localStorage.resolvePath(objectKey);
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        byte[] stored = Files.readAllBytes(file);
        byte[] body = encryptionService.decryptFile(stored);
        String contentType = probeContentType(objectKey, body);

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .contentType(MediaType.parseMediaType(contentType))
                .body(body);
    }

    private String probeContentType(String objectKey, byte[] body) {
        String lower = objectKey.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
