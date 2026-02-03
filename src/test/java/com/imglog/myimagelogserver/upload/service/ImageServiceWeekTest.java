package com.imglog.myimagelogserver.upload.service;

import com.imglog.myimagelogserver.image.domain.ImageItem;
import com.imglog.myimagelogserver.image.dto.WeekImagesResponse;
import com.imglog.myimagelogserver.image.repository.ImageItemRepository;
import com.imglog.myimagelogserver.image.service.ImageService;
import com.imglog.myimagelogserver.image.storage.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * 업로드한 이미지가 있는 요일만 return 하는지 검증 단위테스트
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceWeekTest {

    @Mock
    ImageItemRepository repo;

    @Mock
    StoragePort storage;

    @InjectMocks
    ImageService service;

    @Test
    void getThisWeekGrouped_excludes_empty_days_and_groups_by_date() throws Exception {
        ZoneId zone = ZoneId.of("Asia/Seoul");

        LocalDate monday = LocalDate.now(zone).with(DayOfWeek.MONDAY);
        LocalDate wednesday = monday.plusDays(2);

        ImageItem m1 = newLocalImage(1L, "m1.jpg", monday.atTime(10, 0));
        ImageItem m2 = newLocalImage(2L, "m2.jpg", monday.atTime(9, 0));
        ImageItem w1 = newLocalImage(3L, "w1.jpg", wednesday.atTime(8, 0));

        given(repo.findByUserIdAndCreatedAtBetween(Mockito.eq(1L), Mockito.any(), Mockito.any()))
                .willReturn(List.of(m1, m2, w1));

        WeekImagesResponse res = service.getThisWeekGrouped(1L, zone);

        assertThat(res.weekStart().getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(res.weekEnd()).isEqualTo(res.weekStart().plusDays(6));

        assertThat(res.days()).hasSize(2);
        assertThat(res.days().get(0).day()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(res.days().get(1).day()).isEqualTo(DayOfWeek.WEDNESDAY);

        var monImages = res.days().get(0).images();
        assertThat(monImages).hasSize(2);
        assertThat(monImages.get(0).originalName()).isEqualTo("m2.jpg");
        assertThat(monImages.get(1).originalName()).isEqualTo("m1.jpg");

    }

    /**
     * 날짜 고정을 위해 reflection으로 접근 세팅
     */
    private ImageItem newLocalImage(Long id, String name, LocalDateTime createdAt) throws Exception {
        ImageItem item = ImageItem.ofLocal(1L, "http://localhost/files/" + name, "key_" + name, name, 100);

        setField(item, "id", id);
        setField(item, "createdAt", createdAt);

        return item;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }


}
