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
     * n8n에서 호출: 오늘의 뉴스 일괄 저장
     */
    @PostMapping("/today")
    public ResponseEntity<Map<String, Object>> saveTodayNews(@RequestBody SaveNewsBatchRequest request) {
        int count = service.saveToday(request.news());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "savedCount", count
        ));
    }

    /**
     * 앱에서 호출: 오늘의 뉴스 조회
     */
    @GetMapping("/today")
    public ResponseEntity<TodayNewsResponse> getTodayNews(
            @RequestParam(defaultValue = "true") boolean autoFetch
            ) {
        if (autoFetch) {
            return ResponseEntity.ok(service.getTodayNewsWithFetch());
        } else {
            return ResponseEntity.ok(service.getTodayNews());
        }
    }

}
