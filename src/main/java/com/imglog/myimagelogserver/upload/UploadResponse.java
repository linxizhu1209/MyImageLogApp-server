package com.imglog.myimagelogserver.upload;

import java.util.List;

public record UploadResponse(
        String uploadId,
        String status,
        List<UploadItem> items
) {
    public record UploadItem(
            String originalName,
            String url,
            long size
    ) {}
}
