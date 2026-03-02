package com.imglog.myimagelogserver.news.repository;

import java.util.List;

/**
 * 뉴스 관련 Dto 모음
 */
public class StockNewsDtos {

    /**
     * n8n에서 뉴스 저장 요청 시 사용하는 DTO
     */
    public record SaveNewsRequest(
            String title,
            String summary,
            String sourceUrl,
            String source
    ) {}

    /**
     * n8n에서 여러 뉴스를 한 번에 저장할 때 사용
     */
    public record SaveNewsBatchRequest(
            List<SaveNewsRequest> news
    ) {}

    /**
     * 앱에 뉴스 목록 응답 시 사용하는 DTO
     */
    public record NewsResponse(
            Long id,
            String title,
            String summary,
            String sourceUrl,
            String source
    ) {}

    /**
     * 오늘의 뉴스 목록 응답
     */
    public record TodayNewsResponse(
            String date,
            List<NewsResponse> news
    ) {}
}
