package com.imglog.myimagelogserver.praise.repository;


import com.imglog.myimagelogserver.praise.domain.AppPraise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPraiseRepository extends JpaRepository<AppPraise, Long> {
    Page<AppPraise> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
