package com.imglog.myimagelogserver.upload.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @TempDir
    static Path tempDir;

    static Path uploadBaseDir;

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        uploadBaseDir = tempDir.resolve("uploads");

        r.add("app.upload.base-dir", () -> uploadBaseDir.toString());
        r.add("app.upload.public-url-base", () -> "http://localhost:8080/files");
    }

    @Test
    void multipartUpload_returnsUrls_andStoresFiles() throws Exception {
        MockMultipartFile titlePart = new MockMultipartFile(
                "title", "", "text/plain", "t".getBytes()
        );
        MockMultipartFile contentPart = new MockMultipartFile(
                "content", "", "text/plain", "c".getBytes()
        );
        MockMultipartFile f1 = new MockMultipartFile(
                "files", "a.jpg", "image/jpeg", "AAA".getBytes()
        );
        MockMultipartFile f2 = new MockMultipartFile(
                "files", "b.jpg", "image/jpeg", "BBBB".getBytes()
        );

        mvc.perform(
                        multipart("/uploads/")
                                .file(titlePart)
                                .file(contentPart)
                                .file(f1)
                                .file(f2)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.uploadId", notNullValue()))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].url", containsString("/files/")))
                .andExpect(jsonPath("$.items[1].url", containsString("/files/")));

        // 실제 파일이 temp 업로드 폴더에 저장됐는지 확인
        assertThat(Files.exists(uploadBaseDir)).isTrue();
        // uploadId 폴더가 1개 이상 생김
        assertThat(Files.list(uploadBaseDir).anyMatch(Files::isDirectory)).isTrue();

    }


}
