package com.imglog.myimagelogserver.image.controller;

import com.imglog.myimagelogserver.image.dto.PostDetailResponse;
import com.imglog.myimagelogserver.image.dto.UpdatePostRequest;
import com.imglog.myimagelogserver.image.service.ImageService;
import org.springframework.web.bind.annotation.*;

import java.awt.*;

/**
 * 이미지에 연결된 글(제목/내용) 조회 및 수정 API
 */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final ImageService service;

    public PostController(ImageService service) {
        this.service = service;
    }

    /**
     * 이미지 id로 글 조회
     */
    @GetMapping("/{id}")
    public PostDetailResponse getPostDetail(@PathVariable Long id) {
        return service.getPostDetail(id);
    }

    /**
     * 이미지 id로 글 수정
     */
    @PutMapping("/{id}")
    public void updatePost(
            @PathVariable Long id,
            @RequestBody UpdatePostRequest request
            ) {
        service.updatePost(id, request.title(), request.content());
    }
}
