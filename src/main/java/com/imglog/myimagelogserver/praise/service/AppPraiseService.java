package com.imglog.myimagelogserver.praise.service;

import com.imglog.myimagelogserver.praise.domain.AppPraise;
import com.imglog.myimagelogserver.praise.dto.AppPraiseDtos.PraiseListResponse;
import com.imglog.myimagelogserver.praise.repository.AppPraiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.imglog.myimagelogserver.praise.dto.AppPraiseDtos.*;

@Service
@RequiredArgsConstructor
public class AppPraiseService {

    private static final int MAX_PAGE_SIZE = 50;

    private final AppPraiseRepository repository;

    @Transactional(readOnly = true)
    public PraiseListResponse list(int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<AppPraise> result = repository.findAllByOrderByCreatedAtDesc(pageable);

        return new PraiseListResponse(
                result.getContent().stream().map(this::toItem).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.hasNext()
        );
    }

    @Transactional
    public PraiseItemResponse create(CreateRequest req) {
        String nickname = req.nickname().trim();
        String content = req.content().trim();
        AppPraise saved = repository.save(AppPraise.create(nickname, content));
        return toItem(saved);
    }

    private PraiseItemResponse toItem(AppPraise p) {
        return new PraiseItemResponse(
                p.getId(),
                p.getNickname(),
                p.getContent(),
                p.getCreatedAt()
        );
    }
}
