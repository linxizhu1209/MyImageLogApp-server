package com.imglog.myimagelogserver.upload.controller;

import com.imglog.myimagelogserver.upload.UploadResponse;
import com.imglog.myimagelogserver.upload.service.UploadStorageService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/uploads")
public class UploadController {

    private final UploadStorageService storage;

    @Value("${app.upload.public-url-base:http://localhost:8080/files}")
    private String publicUrlBase;

    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(
            @RequestPart("title") @NotBlank String title,
            @RequestPart("content") @NotBlank String content,
            @RequestPart("files") List<MultipartFile> files
    ) throws IOException {

        if (files == null || files.isEmpty()) {
            return new UploadResponse(null, "NO_FILES", List.of());
        }

        String uploadId = storage.newUploadId();
        Path dir = storage.ensureUploadDir(uploadId);

        List<UploadResponse.UploadItem> items = new ArrayList<>();
        for (MultipartFile f : files) {
            var stored = storage.store(dir, f);
            String url = publicUrlBase + "/" + uploadId + "/" + stored.storedName();
            items.add(new UploadResponse.UploadItem(stored.originalName(), url, stored.size()));
        }

        return new UploadResponse(uploadId, "OK", items);
    }
}
