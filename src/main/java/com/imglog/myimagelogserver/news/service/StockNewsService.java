package com.imglog.myimagelogserver.news.service;

import com.imglog.myimagelogserver.news.domain.DailyNewsSummary;
import com.imglog.myimagelogserver.news.domain.StockNews;
import com.imglog.myimagelogserver.news.repository.DailyNewsSummaryRepository;
import com.imglog.myimagelogserver.news.repository.StockNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static com.imglog.myimagelogserver.news.repository.StockNewsDtos.*;

/**
 * 해외주식 뉴스 비즈니스 로직 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockNewsService {

    private static final ZoneId KOREA = ZoneId.of("Asia/Seoul");

    private final StockNewsRepository repository;
    private final DailyNewsSummaryRepository summaryRepository;
    private final RestTemplate restTemplate;

    @Value("${app.n8n.webhook-url}")
    private String n8nWebhookUrl;

    /**
     * 오늘 날짜의 뉴스를 일괄 저장 (기존 데이터 삭제 후 저장)
     */
    @Transactional
    public int saveToday(List<SaveNewsRequest> newsRequests, String aiSummary) {
        LocalDate today = LocalDate.now(KOREA);

        // 기존의 오늘 뉴스 삭제 (중복 방지)
        repository.deleteByNewsDate(today);

        // 새 뉴스 저장
        List<StockNews> entities = newsRequests.stream()
                .map(req -> StockNews.of(
                        req.title(),
                        req.summary(),
                        req.sourceUrl(),
                        req.source(),
                        today
                ))
                .toList();
        repository.saveAll(entities);

        // AI 요약 저장/업데이트
        if (aiSummary != null && !aiSummary.isBlank()) {
            summaryRepository.findBySummaryDate(today).ifPresentOrElse(
                    existing -> existing.updateSummary(aiSummary),
                    () -> summaryRepository.save(DailyNewsSummary.of(today, aiSummary))
            );
        }

        return entities.size();
    }

    /**
     * 오늘의 뉴스 조회
     */
    public TodayNewsResponse getTodayNews() {
        LocalDate today = LocalDate.now(KOREA);
        List<StockNews> newsList = repository.findByNewsDateOrderByCreatedAtDesc(today);

        String aiSummary = summaryRepository.findBySummaryDate(today)
                .map(DailyNewsSummary::getAiSummary)
                .orElse(null);

        List<NewsResponse> responses = newsList.stream()
                .map(n -> new NewsResponse(
                        n.getId(),
                        n.getTitle(),
                        n.getSummary(),
                        n.getSourceUrl(),
                        n.getSource()
                )).toList();

        return new TodayNewsResponse(today.toString(), aiSummary, responses);
    }

    /**
     * 오늘의 뉴스 조회 (없으면 n8n 워크플로 실행 후 반환)
     */
    public TodayNewsResponse getTodayNewsWithFetch() {
        LocalDate today = LocalDate.now(KOREA);

        // 1. DB에 오늘 뉴스가 있는지 확인
        List<StockNews> existingNews = repository.findByNewsDateOrderByCreatedAtDesc(today);

        if (existingNews.isEmpty()) {
            // 배포/운영 기본: n8n 스케줄(08:00)로 채워짐. 여기서는 더 이상 자동 실행하지 않음.
            return new TodayNewsResponse(today.toString(), null, List.of());
        }
        log.info("DB에서 기존 뉴스 {}개 반환", existingNews.size());

        // 4. 응답 생성
        String aiSummary = summaryRepository.findBySummaryDate(today)
                .map(DailyNewsSummary::getAiSummary)
                .orElse(null);

        List<NewsResponse> responses = existingNews.stream()
                .map(n -> new NewsResponse(
                        n.getId(),
                        n.getTitle(),
                        n.getSummary(),
                        n.getSourceUrl(),
                        n.getSource()
                ))
                .toList();

        return new TodayNewsResponse(today.toString(), aiSummary, responses);
    }
}
