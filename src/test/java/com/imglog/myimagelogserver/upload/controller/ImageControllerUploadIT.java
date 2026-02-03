package com.imglog.myimagelogserver.upload.controller;

import com.imglog.myimagelogserver.image.service.StoredFile;
import com.imglog.myimagelogserver.image.storage.StoragePort;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerUploadIT {

    @Autowired MockMvc mvc;

    @MockitoBean
    StoragePort storagePort;

    @Test
    void upload_multiple_files_returns_OK_and_items() throws Exception {
        given(storagePort.store(any()))
                .willReturn(new StoredFile(
                        "LOCAL",         // storageType
                        null,
                        "a_1.jpg",        // objectKey
                        "http://localhost:8080/files/a_1.jpg", // url
                        5L,              // size
                        "a.jpg"          // originalName
                ))
                .willReturn(new StoredFile(
                        "LOCAL",
                        null,
                        "b_1.jpg",
                        "http://localhost:8080/files/b_1.jpg",
                        6L,
                        "b.jpg"
                ));

        MockMultipartFile f1 = new MockMultipartFile(
                "files",
                "a.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy".getBytes() // 5 bytes
        );

        MockMultipartFile f2 = new MockMultipartFile(
                "files",
                "b.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy2".getBytes() // 6 bytes
        );

        // when & then
        mvc.perform(multipart("/api/images/upload")
                        .file(f1)
                        .file(f2)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.items", Matchers.hasSize(2)))

                // 1번째 item 검증
                .andExpect(jsonPath("$.items[0].url").value("http://localhost:8080/files/a_1.jpg"))
                .andExpect(jsonPath("$.items[0].originalName").value("a.jpg"))
                .andExpect(jsonPath("$.items[0].size").value(5))

                // 2번째 item 검증
                .andExpect(jsonPath("$.items[1].url").value("http://localhost:8080/files/b_1.jpg"))
                .andExpect(jsonPath("$.items[1].originalName").value("b.jpg"))
                .andExpect(jsonPath("$.items[1].size").value(6));
    }
}