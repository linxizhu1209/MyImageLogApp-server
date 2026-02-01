package com.imglog.myimagelogserver.upload.service;

public record StoredFile(
        String originalName,
        String storedName,
        long size
) {
}
