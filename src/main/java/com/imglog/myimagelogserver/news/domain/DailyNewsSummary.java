package com.imglog.myimagelogserver.news.domain;

import com.imglog.myimagelogserver.image.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "daily_news_summary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyNewsSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate summaryDate;

    @Column(length = 3000)
    private String aiSummary;

    public static DailyNewsSummary of(LocalDate date, String aiSummary) {
        DailyNewsSummary entity = new DailyNewsSummary();
        entity.summaryDate = date;
        entity.aiSummary = aiSummary;
        return entity;
    }

    public void updateSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }
}
