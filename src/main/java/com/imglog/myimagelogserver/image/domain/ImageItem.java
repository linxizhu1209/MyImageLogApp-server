package com.imglog.myimagelogserver.image.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "images")
public class ImageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StorageType storageType;

    @Column(length = 100)
    private String bucket;

    @Column(length = 500)
    private String objectKey;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false, length = 500)
    private String originalName;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ImageItem() {}

    public static ImageItem ofLocal(
            Long userId,
            String url,
            String objectKey,
            String originalName,
            long size
    ) {
        ImageItem i = new ImageItem();
        i.userId = userId;
        i.storageType = StorageType.LOCAL;
        i.url = url;
        i.objectKey = objectKey;
        i.originalName = originalName;
        i.size = size;
        i.createdAt = LocalDateTime.now();
        return i;
    }

    public enum StorageType {
        LOCAL, S3
    }
}
