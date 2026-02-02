package com.imglog.myimagelogserver.image.repository;

import com.imglog.myimagelogserver.image.domain.ImageItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ImageItemRepository extends JpaRepository<ImageItem, Long> {

    List<ImageItem> findByUserIdAndCreatedAtBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
