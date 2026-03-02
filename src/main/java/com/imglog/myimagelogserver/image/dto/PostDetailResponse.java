package com.imglog.myimagelogserver.image.dto;

import java.time.LocalDateTime;

public record PostDetailResponse (
        Long id,
        String title,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedA
) {}