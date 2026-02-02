package com.imglog.myimagelogserver.image.controller;

import com.imglog.myimagelogserver.image.domain.ImageItem;
import com.imglog.myimagelogserver.image.dto.*;
import com.imglog.myimagelogserver.image.service.ImageService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService service;

    public ImageController(ImageService service) {
        this.service = service;
    }

    /**
     * multipart 업로드 + DB 저장
     */

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResult upload(
            @RequestParam @NotNull Long userId,
            @RequestPart("files") List<MultipartFile> files
    ) {
        List<ImageItem> saved = service.upload(userId, files);

        List<UploadItem> items = saved.stream()
                .map(i -> new UploadItem(i.getId(), i.getUrl(), i.getOriginalName(), i.getSize()))
                .toList();
        return new UploadResult("OK", items);
    }

    /**
     * 이번 주(월~일) 이미지 목록
     */
    @GetMapping("/week")
    public WeekResult week(@RequestPart @NotNull Long userId) {
        List<ImageItem> list = service.getThisWeekImages(userId, ZoneId.of("Asia/Seoul"));

        List<WeekItem> items = list.stream()
                .map(i -> new WeekItem(i.getId(), i.getUrl(), i.getCreatedAt().toString())).toList();

        return new WeekResult("OK", items);
    }
}
