package com.imglog.myimagelogserver.image.dto;

import java.util.List;

public record WeekResult(
        String status,
        List<WeekItem> items
) {
}
