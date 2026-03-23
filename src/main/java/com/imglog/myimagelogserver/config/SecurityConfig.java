package com.imglog.myimagelogserver.config;

import com.imglog.myimagelogserver.auth.CustomOAuth2UserService;
import com.imglog.myimagelogserver.auth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))  // H2 console
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/favicon.ico").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/files/**").permitAll()
                        .requestMatchers("/api/**").permitAll()  //  JWT 필터로 보호 예정
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
}
