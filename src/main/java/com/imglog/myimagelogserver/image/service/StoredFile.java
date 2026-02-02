package com.imglog.myimagelogserver.image.service;

public record StoredFile(
        String storageType, // "Local" , "S3"
        String bucker, // S3라면 채우고, LOCAL이면 Null
        String objectKey, // 저장 키 (파일명)
        String url,
        long size,
        String originalName
) {
}
