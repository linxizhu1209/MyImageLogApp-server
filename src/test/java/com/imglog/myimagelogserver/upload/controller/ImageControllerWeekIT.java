package com.imglog.myimagelogserver.upload.controller;

import com.imglog.myimagelogserver.image.domain.ImageItem;
import com.imglog.myimagelogserver.image.repository.ImageItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.servlet.ServletWebServerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class ImageControllerWeekIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ImageItemRepository repo;

    private final ZoneId zone = ZoneId.of("Asia/Seoul");
    @Autowired
    private ServletWebServerFactory servletWebServerFactory;

    @BeforeEach
    void setUp() {
        repo.deleteAll();

        LocalDate monday = LocalDate.now(zone).with(DayOfWeek.MONDAY);

        ImageItem item = ImageItem.ofLocal(
                1L,
                "http://localhost:8080/files/test.jpg",
                "key_test.jpg",
                "test.jpg",
                1234
        );

        try {
            setField(item, "createdAt", monday.atTime(12, 0));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        repo.save(item);
    }

    @Test
    void week_returns_only_non_empty_days() throws Exception {
        mvc.perform(get("/api/images/week").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").exists())
                .andExpect(jsonPath("$.weekEnd").exists())
                .andExpect(jsonPath("$.days", hasSize(1)))
                .andExpect(jsonPath("$.days[0].day").value("MONDAY"))
                .andExpect(jsonPath("$.days[0].images", hasSize(1)))
                .andExpect(jsonPath("$.days[0].images[0].originalName").value("test.jpg"))
                .andExpect(jsonPath("$.days[0].images[0].url").value("http://localhost:8080/files/test.jpg"));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }



}
