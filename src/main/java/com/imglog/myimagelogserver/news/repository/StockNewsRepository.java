package com.imglog.myimagelogserver.news.repository;

import com.imglog.myimagelogserver.news.domain.StockNews;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * 해외주식 뉴스 데이터 접근 레포지토리
 */
public interface StockNewsRepository extends JpaRepository<StockNews, Long> {

    /**
     * 특정 날짜의 뉴스 목록 조회 (최신순)
     */
    List<StockNews> findByNewsDateOrderByCreatedAtDesc(LocalDate newsDate);

    /**
     * 특정 날짜의 뉴스 삭제 (새로 갱신 전 기존 데이터 삭제용)
     */
    void deleteByNewsDate(LocalDate newsDate);

    /**
     * 특정 날짜의 뉴스 존재 여부
     */
    boolean existsByNewsDate(LocalDate newsDate);
}
