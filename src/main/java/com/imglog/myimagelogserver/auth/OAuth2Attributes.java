package com.imglog.myimagelogserver.auth;

import com.imglog.myimagelogserver.user.domain.User;

import java.util.Map;

/**
 * OAuth2 제공자별 사용자 속성 추출
 */
public record OAuth2Attributes(
        User.OAuthProvider provider,
        String oauthId,
        String email,
        String nickname,
        String profileImageUrl
) {
    public static OAuth2Attributes of(String providerId, Map<String, Object> attributes) {
        return switch (providerId.toLowerCase()) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자: " + providerId);
        };
    }

    private static OAuth2Attributes ofGoogle(Map<String, Object> attributes) {
        return new OAuth2Attributes(
                User.OAuthProvider.GOOGLE,
                (String) attributes.get("sub"),
                (String) attributes.get("email"),
                (String) attributes.get("name"),
                (String) attributes.get("picture")
        );
    }

    @SuppressWarnings("unchecked")
    private static OAuth2Attributes ofKakao(Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) account.getOrDefault("profile", Map.of());

        String email = (String) account.get("email");
        String nickname = (String) profile.get("nickname");
        String profileImage = (String) profile.get("profile_image_url");

        return new OAuth2Attributes(
                User.OAuthProvider.KAKAO,
                String.valueOf(attributes.get("id")),
                email != null ? email : "",
                nickname != null ? nickname : "카카오사용자",
                profileImage
        );
    }
}
