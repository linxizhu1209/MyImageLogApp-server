package com.imglog.myimagelogserver.news.domain;

import com.imglog.myimagelogserver.image.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 해외주식 뉴스 엔티티 - 오늘의 해외주식 top10 주요 뉴스를 저장
 */
@Entity
@Table(name="stock_news")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockNews extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 2000)
    private String summary;

    @Column(length = 1000)
    private String sourceUrl;

    @Column(length = 100)
    private String source;

    @Column(nullable = false)
    private LocalDate newsDate;

    public static StockNews of(String title, String summary, String sourceUrl, String source, LocalDate newsDate) {
        StockNews news = new StockNews();
        news.title = title;
        news.summary = summary;
        news.sourceUrl = sourceUrl;
        news.source = source;
        news.newsDate = newsDate;
        return news;
    }
}
