package com.imglog.myimagelogserver.image.storage;

import com.imglog.myimagelogserver.image.service.StoredFile;
import com.imglog.myimagelogserver.security.crypto.EncryptionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class LocalStorage implements StoragePort {

    private final Path baseDir;
    private final String publicUrlBase;
    private final EncryptionService encryptionService;

    public LocalStorage(
            @Value("${app.upload.base-dir:uploads}") String baseDir,
            @Value("${app.upload.public-url-base:http://localhost:8080/files}") String publicUrlBase,
            EncryptionService encryptionService
    ) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
        this.publicUrlBase = publicUrlBase.endsWith("/") ? publicUrlBase.substring(0, publicUrlBase.length() - 1) : publicUrlBase;
        this.encryptionService = encryptionService;
    }

    @Override
    public StoredFile store(MultipartFile file) throws IOException {
        Files.createDirectories(baseDir);

        String original = file.getOriginalFilename();
        String safeOriginal = sanitize(original);
        String objectKey = UUID.randomUUID() + "_" + safeOriginal;

        byte[] plain = file.getBytes();
        byte[] toWrite = encryptionService.encryptFile(plain);
        Path target = baseDir.resolve(objectKey).normalize();
        Files.write(target, toWrite);

        String url = publicUrlBase + "/" + objectKey;

        return new StoredFile(
                "Local",
                null,
                objectKey,
                url,
                file.getSize(),
                (StringUtils.hasText(original) ? original : safeOriginal)
        );
    }

    public Path resolvePath(String objectKey) {
        Path resolved = baseDir.resolve(objectKey).normalize();
        if (!resolved.startsWith(baseDir)) {
            throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        }
        return resolved;
    }

    public Path baseDir() {
        return baseDir;
    }

    private String sanitize(String original) {
        String name = StringUtils.hasText(original) ? original : "file";
        name = name.replace("\\", "_").replace("/", "_");
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
