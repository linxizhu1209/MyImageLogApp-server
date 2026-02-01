package com.imglog.myimagelogserver.upload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class UploadStorageService {

    private final Path baseDir;

    public UploadStorageService(@Value("${app.upload.base-dir:uploads}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    public String newUploadId() {
        return "u-" + UUID.randomUUID();
    }

    public Path ensureUploadDir(String uploadId) throws IOException {
        Path dir = baseDir.resolve(uploadId).normalize();
        Files.createDirectories(dir);
        return dir;
    }

    public StoredFile store(Path uploadDir, MultipartFile file) throws IOException
    {
      String original = file.getOriginalFilename();
      String safeName =toSafeFilename(original);

      Path target = uploadDir.resolve(safeName).normalize();

      Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

      return new StoredFile(original == null ? safeName : original, safeName, file.getSize());
    }

    private String toSafeFilename(String original) {
        String name = StringUtils.hasText(original) ? original : "file";

        name = name.replace("\\", "_").replace("/", "_");
        while (name.contains("..")) {
            name = name.replace("..", "_");
        }

        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}


