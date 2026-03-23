package com.imglog.myimagelogserver.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OAuth2User + userId(우리 DB) 래퍼
 */
public class AppOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final Long userId;

    public AppOAuth2User(OAuth2User delegate, Long userId) {
        this.delegate = delegate;
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
