package com.imglog.myimagelogserver.user.domain;

import com.imglog.myimagelogserver.image.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"oauth_provider", "oauth_id"})
})
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private OAuthProvider oauthProvider;

    @Column(name = "oauth_id", nullable = false, length = 255)
    private String oauthId;

    @Column(length = 255)
    private String email;

    @Column(length = 100)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    protected User() {}

    public static User ofOAuth(OAuthProvider provider, String oauthId, String email, String nickname, String profileImageUrl) {
        User u = new User();
        u.oauthProvider = provider;
        u.oauthId = oauthId;
        u.email = email;
        u.nickname = nickname != null ? nickname : email;
        u.profileImageUrl = profileImageUrl;
        return u;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) this.nickname = nickname;
        if (profileImageUrl != null) this.profileImageUrl = profileImageUrl;
    }

    public enum OAuthProvider {
        GOOGLE, KAKAO
    }
}
