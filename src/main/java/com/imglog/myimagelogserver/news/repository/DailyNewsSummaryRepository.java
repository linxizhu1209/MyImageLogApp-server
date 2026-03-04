package com.imglog.myimagelogserver.news.repository;

import com.imglog.myimagelogserver.news.domain.DailyNewsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyNewsSummaryRepository extends JpaRepository<DailyNewsSummary, Long> {
    Optional<DailyNewsSummary> findBySummaryDate(LocalDate date);
    void deleteBySummaryDate(LocalDate date);
}
