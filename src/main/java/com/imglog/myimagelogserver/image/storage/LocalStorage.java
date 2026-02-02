package com.imglog.myimagelogserver.image.storage;

import com.imglog.myimagelogserver.image.service.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class LocalStorage implements StoragePort{

    private final Path baseDir;
    private final String publicUrlBase;

    public LocalStorage(
            @Value("${app.upload.base-dir:uploads}") String baseDir,
            @Value("${app.upload.public-url-base:http://localhost:8000/files}") String publicUrlBase
    ) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
        this.publicUrlBase = publicUrlBase;
    }

    @Override
    public StoredFile store(MultipartFile file) throws IOException {
        Files.createDirectories(baseDir);

        String original = file.getOriginalFilename();
        String safeOriginal = sanitize(original);

        // 저장 키 (파일명) : UUID + 원본파일명
        String objectKey = UUID.randomUUID() + "_" + safeOriginal;

        Path target = baseDir.resolve(objectKey).normalize();
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String url = publicUrlBase + "/" +objectKey;

        return new StoredFile(
                "Local",
                null,
                objectKey,
                url,
                file.getSize(),
                (StringUtils.hasText(original) ? original : safeOriginal)
        );
    }

    private String sanitize(String original) {
        String name = StringUtils.hasText(original) ? original : "file";
        name = name.replace("\\", "_").replace("/", "_");
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
