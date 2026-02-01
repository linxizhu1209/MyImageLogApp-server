package com.imglog.myimagelogserver.upload.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UploadStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void ensureUploadDir_createsDirectory() throws IOException {
        UploadStorageService service = new UploadStorageService(tempDir.toString());
        String uploadId = service.newUploadId();
        Path dir = service.ensureUploadDir(uploadId);

        assertThat(Files.exists(dir)).isTrue();
        assertThat(Files.isDirectory(dir)).isTrue();
    }

    @Test
    void store_sanitizesFilename_andSavesFiles() throws IOException {
        UploadStorageService service = new UploadStorageService(tempDir.toString());

        String uploadId = "u-test";
        Path dir = service.ensureUploadDir(uploadId);

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "../evil/../a b?.jpg",
                "image/jpeg",
                "hello".getBytes()
        );

        var stored = service.store(dir, file);

        assertThat(stored.storedName()).doesNotContain("..");
        assertThat(stored.storedName()).doesNotContain("/");
        assertThat(stored.storedName()).doesNotContain("\\");
        assertThat(stored.storedName()).isNotBlank();

        Path saved = dir.resolve(stored.storedName());
        assertThat(Files.exists(saved)).isTrue();
        assertThat(Files.size(saved)).isEqualTo(5);
    }

}