package com.imglog.myimagelogserver.stockreport.repository;


import com.imglog.myimagelogserver.stockreport.domain.StockReportSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockReportSubscriptionRepository extends JpaRepository<StockReportSubscription, Long> {
    Optional<StockReportSubscription> findByUserId(Long userId);
    List<StockReportSubscription> findByEnabledTrue();
}
