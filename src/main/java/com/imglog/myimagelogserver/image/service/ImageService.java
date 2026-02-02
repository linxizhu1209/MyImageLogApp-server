package com.imglog.myimagelogserver.image.service;

import com.imglog.myimagelogserver.image.domain.ImageItem;
import com.imglog.myimagelogserver.image.dto.DayImages;
import com.imglog.myimagelogserver.image.dto.WeekImagesResponse;
import com.imglog.myimagelogserver.image.repository.ImageItemRepository;
import com.imglog.myimagelogserver.image.storage.StoragePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ImageService {

    private final ImageItemRepository repo;
    private final StoragePort storage;

    public ImageService(ImageItemRepository repo, StoragePort storage) {
        this.repo = repo;
        this.storage = storage;
    }

    /**
     * 파일 저장 + DB 메타데이터 저장
     */
    @Transactional
    public List<ImageItem> upload(Long userId, List<MultipartFile> files) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("files are required");
        }

        try {
            return files.stream().map(file -> {
                try {
                    StoredFile stored = storage.store(file);

                    ImageItem entity = ImageItem.ofLocal(
                            userId,
                            stored.url(),
                            stored.objectKey(),
                            stored.originalName(),
                            stored.size()
                    );
                    return repo.save(entity);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store file", e);
                }
            }).toList();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * 해당 주 (월~일)의 이미지 조회
     */
    @Transactional(readOnly = true)
    public WeekImagesResponse getThisWeekGrouped(Long userId, ZoneId zoneId) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (zoneId == null) zoneId = ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(zoneId);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekend = weekStart.plusDays(6);

        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekend.plusDays(1).atStartOfDay().minusNanos(1);

        List<ImageItem> items = repo.findByUserIdAndCreatedAtBetween(userId, start, end);

        Map<LocalDate, List<ImageItem>> byDate = items.stream()
                .collect(Collectors.groupingBy(i -> i.getCreatedAt().toLocalDate()));

        List<DayImages> days = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate d = weekStart.plusDays(i);
            List<ImageItem> list = byDate.getOrDefault(d, List.of());
            if (list.isEmpty()) continue;

            List<DayImages.ImageSummary> summaries = list.stream()
                    .sorted(Comparator.comparing(ImageItem::getCreatedAt))
                    .map(x -> new DayImages.ImageSummary(
                            x.getId(),
                            x.getUrl(),
                            x.getOriginalName(),
                            x.getSize(),
                            x.getCreatedAt().toString()
                    ))
                    .toList();

            days.add(new DayImages(d.getDayOfWeek(), d, summaries));
        }
        return new WeekImagesResponse(weekStart, weekend, days);
    }
}


