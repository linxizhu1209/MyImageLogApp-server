package com.imglog.myimagelogserver.image.dto;

import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.util.List;

/**
 * 월~일 주간 리포트 응답
 */
public record WeekImagesResponse(
        LocalDate weekStart,
        LocalDate weekEnd,
        List<DayImages> days
) {
}
