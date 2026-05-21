package com.imglog.myimagelogserver.praise.controller;

import com.imglog.myimagelogserver.praise.dto.AppPraiseDtos;
import com.imglog.myimagelogserver.praise.dto.AppPraiseDtos.PraiseItemResponse;
import com.imglog.myimagelogserver.praise.dto.AppPraiseDtos.PraiseListResponse;
import com.imglog.myimagelogserver.praise.service.AppPraiseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app-praises")
@RequiredArgsConstructor
public class AppPraiseController {

    private final AppPraiseService service;

    /**
     * 칭찬(후기) 목록 - 비로그인 허용
     */
    @GetMapping
    public PraiseListResponse list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return service.list(page, size);
    }

    /**
     * 칭찬(후기) 등록 - 비로그인 허용
     */
    @PostMapping
    public ResponseEntity<PraiseItemResponse> create(@Valid @RequestBody AppPraiseDtos.CreateRequest body) {
        PraiseItemResponse created = service.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
