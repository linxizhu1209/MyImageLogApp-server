package com.imglog.myimagelogserver.stockreport.dto;

import java.util.List;

public class StockReportDtos {

    public record UpsertRequest(
            String email,
            List<String> symbols,
            Integer sendHour,
            Boolean enabled
    ) {}

    public record SubscriptionResponse(
            Long id,
            String email,
            List<String> symbols, // 주식 목록
            int sendHour,
            boolean enabled
    ) {}

    /** n8n이 읽는 형태 */
    public record ActiveSubscriptionResponse(
            Long subscriptionId,
            Long userId,
            String email,
            List<String> symbols
    ) {}
}
