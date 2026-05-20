package com.imglog.myimagelogserver.stockreport.controller;

import com.imglog.myimagelogserver.auth.JwtAuthenticationPrincipal;
import com.imglog.myimagelogserver.stockreport.dto.StockReportDtos;
import com.imglog.myimagelogserver.stockreport.dto.StockReportDtos.SubscriptionResponse;
import com.imglog.myimagelogserver.stockreport.service.StockReportSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StockReportSubscriptionController {

    private final StockReportSubscriptionService service;

    @Value("${app.n8n.api-key:changeme}")
    private String n8nApiKey;

    /**
     * 구독 저장 및 수정
     */
    @PutMapping("/api/stock-report-subscription/me")
    public SubscriptionResponse upsert(Authentication auth, @RequestBody StockReportDtos.UpsertRequest body) {
        Long userId = requireUserId(auth);
        return service.upsert(userId, body);
    }

    /**
     * 내 구독 조회
     */
    @GetMapping("/api/stock-report-subscriptions/me")
    public SubscriptionResponse getMine(Authentication auth) {
        Long userId = requireUserId(auth);
        return service.getMine(userId);
    }

    /**
     * 구독 해지
     */
    @DeleteMapping("/api/stock-report-subscriptions/me")
    public ResponseEntity<Map<String, Object>> disable(Authentication auth) {
        Long userId = requireUserId(auth);
        service.disable(userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * n8n: 활성 구독 목록
     */
    @GetMapping("/api/stock-report-subscriptions/active")
    public List<StockReportDtos.ActiveSubscriptionResponse> listActive(
            @RequestHeader(value= "X-N8N-API-Key", required = false) String key
    ) {
        if (key == null || !key.equals(n8nApiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        return service.listActive();
    }

    private Long requireUserId(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof JwtAuthenticationPrincipal p)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return p.userId();
    }
}
