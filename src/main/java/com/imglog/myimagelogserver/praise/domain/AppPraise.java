package com.imglog.myimagelogserver.praise.domain;

import com.imglog.myimagelogserver.image.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_praise")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppPraise extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 30)
    private String nickname;
    @Column(nullable = false, length = 500)
    private String content;
    public static AppPraise create(String nickname, String content) {
        AppPraise p = new AppPraise();
        p.nickname = nickname;
        p.content = content;
        return p;
    }
}
