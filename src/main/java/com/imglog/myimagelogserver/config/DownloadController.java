package com.imglog.myimagelogserver.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class DownloadController {

    private static final String APK_LOCATION = "static/downloads/MyImageLogApp.apk";
    private static final String APK_FILENAME = "MyImageLogApp.apk";
    private static final MediaType APK_MEDIA_TYPE =
            MediaType.parseMediaType("application/vnd.android.package-archive");

    @GetMapping("/download/apk")
    public ResponseEntity<?> downloadApk() throws IOException {
        Resource resource = new ClassPathResource(APK_LOCATION);

        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.status(404)
                    .body("APK file not found. Please upload " + APK_LOCATION + " and redeploy.");
        }

        return ResponseEntity.ok()
                .contentType(APK_MEDIA_TYPE)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + APK_FILENAME + "\"")
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
