package com.imglog.myimagelogserver.news.controller;

import com.imglog.myimagelogserver.news.service.StockNewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.imglog.myimagelogserver.news.repository.StockNewsDtos.SaveNewsBatchRequest;
import static com.imglog.myimagelogserver.news.repository.StockNewsDtos.TodayNewsResponse;

/**
 * 해외주식 뉴스 API 컨트롤러
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class StockNewsController {

    private final StockNewsService service;

    /**
     * n8n에서 호출: 오늘의 뉴스 일괄 저장 + AI 요약 저장
     */
    @PostMapping("/today")
    public ResponseEntity<Map<String, Object>> saveTodayNews(@RequestBody SaveNewsBatchRequest request) {
        int count = service.saveToday(request.news(), request.aiSummary());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "savedCount", count,
                "hasAiSummary", request.aiSummary() != null
        ));
    }

    /**
     * 앱에서 호출: 오늘의 뉴스 조회
     */
    @GetMapping("/today")
    public ResponseEntity<TodayNewsResponse> getTodayNews(
            @RequestParam(defaultValue = "true") boolean autoFetch
            ) {
        // 배포/운영에서는 n8n이 스케줄로 채우는 구조가 기본.
        // autoFetch=true는 개발 편의용으로만 남겨두고, Kotlin 앱에서는 false로 호출 권장.
        return autoFetch
                ? ResponseEntity.ok(service.getTodayNewsWithFetch())
                : ResponseEntity.ok(service.getTodayNews());
    }

}
