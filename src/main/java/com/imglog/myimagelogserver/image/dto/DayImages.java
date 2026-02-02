package com.imglog.myimagelogserver.image.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record DayImages(
        DayOfWeek day,
        LocalDate date,
        List<ImageSummary> images
) {
    public record ImageSummary(
            Long id,
            String url,
            String originalName,
            long size,
            String createdAt
    ) {}
}
