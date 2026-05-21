package com.imglog.myimagelogserver.praise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class AppPraiseDtos {

    public record CreateRequest(
            @NotBlank(message = "닉네임을 입력해 주세요.")
            @Size(min = 2, max = 10, message = "닉네임은 2~10자입니다.")
            String nickname,

            @NotBlank(message = "후기 내용을 입력해 주세요.")
            @Size(min = 1, max = 500, message = "후기는 500자 이하입니다.")
            String content
    ) {}

    public record PraiseItemResponse(
            Long id,
            String nickname,
            String content,
            LocalDateTime createdAt
    ) {}

    public record PraiseListResponse(
            List<PraiseItemResponse> items,
            int page,
            int size,
            long totalElements,
            boolean hasNext
    ) {}
}
