package com.imglog.myimagelogserver.image.dto;

public record UploadItem(
        Long id,
        String url,
        String originalName,
        long size
) {
}
