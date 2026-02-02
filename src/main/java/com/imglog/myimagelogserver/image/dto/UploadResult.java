package com.imglog.myimagelogserver.image.dto;

import java.util.List;

public record UploadResult(
        String status,
        List<UploadItem> items
) {
}
