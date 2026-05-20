package com.imglog.myimagelogserver.stockreport.service;

import com.imglog.myimagelogserver.stockreport.domain.StockReportSubscription;
import com.imglog.myimagelogserver.stockreport.dto.StockReportDtos;
import com.imglog.myimagelogserver.stockreport.dto.StockReportDtos.SubscriptionResponse;
import com.imglog.myimagelogserver.stockreport.dto.StockReportDtos.UpsertRequest;
import com.imglog.myimagelogserver.stockreport.repository.StockReportSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.asm.TypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static com.imglog.myimagelogserver.stockreport.dto.StockReportDtos.*;

@Service
@RequiredArgsConstructor
public class StockReportSubscriptionService {

    private final StockReportSubscriptionRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional
    public SubscriptionResponse upsert(Long userId, UpsertRequest req) {
        validate(req);
        String json = toJson(req.symbols());
        int hour = req.sendHour() != null ? req.sendHour() : 14;
        boolean enabled = req.enabled() == null || req.enabled();

        StockReportSubscription entity = repository.findByUserId(userId)
                .map(existing -> {
                    existing.update(req.email(), json, hour, enabled);
                    return existing;
                })
                .orElseGet(() -> StockReportSubscription.create(userId, req.email(), json, hour));

        repository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getMine(Long userId) {
        StockReportSubscription entity = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("구독 정보가 없습니다."));
        return toResponse(entity);
    }

    /**
     * 구독 취소
     */
    @Transactional
    public void disable(Long userId) {
        StockReportSubscription entity = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("구독 정보가 없습니다."));
        entity.update(entity.getEmail(), entity.getSymbolsJson(), entity.getSendHour(), false);
    }

    /**
     * n8n 스케줄용
     */
    @Transactional(readOnly = true)
    public List<ActiveSubscriptionResponse> listActive() {
        return repository.findByEnabledTrue().stream()
                .map(e -> new ActiveSubscriptionResponse(
                        e.getId(),
                        e.getUserId(),
                        e.getEmail(),
                        fromJson(e.getSymbolsJson())
                ))
                .toList();
    }

    /**
     * 유효성 검사
     */
    private void validate(UpsertRequest req) {
        if (req.email() == null || req.email().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (req.symbols() == null || req.symbols().isEmpty()) {
            throw  new IllegalArgumentException("종목을 1개 이상 선택하세요.");
        }
        if (req.symbols().size() > 10) {
            throw new IllegalArgumentException("종목은 최대 10개까지 가능합니다.");
        }
    }

    private String toJson(List<String> symbols) {
        try {
            return objectMapper.writeValueAsString(symbols);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> fromJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private SubscriptionResponse toResponse(StockReportSubscription e) {
        return new SubscriptionResponse(
                e.getId(),
                e.getEmail(),
                fromJson(e.getSymbolsJson()),
                e.getSendHour(),
                e.isEnabled()
        );
    }

}
