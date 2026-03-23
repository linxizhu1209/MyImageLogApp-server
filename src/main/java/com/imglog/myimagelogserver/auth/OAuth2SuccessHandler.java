package com.imglog.myimagelogserver.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAuth 로그인 성공 후 JWT 발급하고 앱 리다이렉트 URL로 이동
 * myimagelogapp://auth?token=xxx&userId=xxx
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${app.oauth.redirect-uri:myimagelogapp://auth}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        AppOAuth2User user = (AppOAuth2User) authentication.getPrincipal();
        Long userId = user.getUserId();
        String token = jwtUtil.createToken(userId);

        String url = redirectUri
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&userId=" + userId;

        getRedirectStrategy().sendRedirect(request, response, url);
    }
}
