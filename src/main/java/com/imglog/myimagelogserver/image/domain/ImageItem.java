package com.imglog.myimagelogserver.image.domain;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "images")
public class ImageItem extends BaseEntity {

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

    @Column(length = 200)
    private String title;

    @Column(length = 2000)
    private String content;

    protected ImageItem() {}

    public static ImageItem ofLocal(
            Long userId,
            String url,
            String objectKey,
            String originalName,
            long size,
            String title,
            String content
    ) {
        ImageItem i = new ImageItem();
        i.userId = userId;
        i.storageType = StorageType.LOCAL;
        i.url = url;
        i.objectKey = objectKey;
        i.originalName = originalName;
        i.size = size;
        i.title = title;
        i.content = content;
        return i;
    }

    public void updateTitleAndContent(String title, String content) {
        this.title = title;
        this.content = content;
    }
    public enum StorageType {
        LOCAL, S3
    }
}
