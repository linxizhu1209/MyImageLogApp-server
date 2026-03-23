package com.imglog.myimagelogserver.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * OAuth 로그인 URL 등 인증 관련 API
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${server.port:8080}")
    private String port;

    @Value("${app.api-base-url:}")
    private String apiBaseUrl;

    /**
     * 앱에서 OAuth 로그인 시 열 WebView URL 목록
     * baseUrl + /oauth2/authorization/{provider}
     */
    @GetMapping("/oauth-urls")
    public Map<String, String> oauthUrls() {
        String base = apiBaseUrl != null && !apiBaseUrl.isBlank()
                ? apiBaseUrl
                : "http://localhost:" + port;
        return Map.of(
                "google", base + "/oauth2/authorization/google",
                "kakao", base + "/oauth2/authorization/kakao"
        );
    }
}
