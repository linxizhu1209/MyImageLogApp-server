package com.imglog.myimagelogserver.image.storage;

import com.imglog.myimagelogserver.image.service.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void store_saves_file_to_disk_and_returns_metadata() throws Exception {
        // given
        LocalStorage storage = new LocalStorage(
                tempDir.toString(),
                "http://localhost:8080/files"
        );

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "hello.jpg",
                "image/jpeg",
                "dummy".getBytes() // 5 bytes
        );

        // when
        StoredFile stored = storage.store(file);

        // then (metadata)
        assertThat(stored.storageType()).isEqualTo("Local");
        assertThat(stored.bucker()).isNull();
        assertThat(stored.originalName()).isEqualTo("hello.jpg");
        assertThat(stored.size()).isEqualTo(5L);

        assertThat(stored.objectKey()).contains("_hello.jpg");
        assertThat(stored.url()).isEqualTo("http://localhost:8080/files/" + stored.objectKey());

        // then (file saved)
        Path saved = tempDir.resolve(stored.objectKey()).normalize();

        assertThat(Files.exists(saved))
                .as("saved file should exist: " + saved)
                .isTrue();

        assertThat(Files.size(saved)).isEqualTo(stored.size());
    }
}
