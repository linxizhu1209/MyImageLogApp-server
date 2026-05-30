package com.imglog.myimagelogserver.user.domain;

import com.imglog.myimagelogserver.image.domain.BaseEntity;
import com.imglog.myimagelogserver.security.crypto.EncryptedStringConverter;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"oauth_provider", "oauth_lookup_key"})
})
@Getter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private OAuthProvider oauthProvider;

    /** OAuth 로그인 조회용 HMAC (평문 저장하지 않음) */
    @Column(name = "oauth_lookup_key", length = 64)
    private String oauthLookupKey;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "oauth_id", nullable = false, length = 512)
    private String oauthId;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 512)
    private String nickname;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "profile_image_url", length = 1024)
    private String profileImageUrl;

    protected User() {}

    public static User ofOAuth(
            OAuthProvider provider,
            String oauthLookupKey,
            String oauthId,
            String email,
            String nickname,
            String profileImageUrl
    ) {
        User u = new User();
        u.oauthProvider = provider;
        u.oauthLookupKey = oauthLookupKey;
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

    public void migrateOauthLookupKey(String oauthLookupKey) {
        this.oauthLookupKey = oauthLookupKey;
    }

    public enum OAuthProvider {
        GOOGLE, KAKAO
    }
}
