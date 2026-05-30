package com.imglog.myimagelogserver.user.repository;

import com.imglog.myimagelogserver.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthProviderAndOauthLookupKey(User.OAuthProvider provider, String oauthLookupKey);

    /** 마이그레이션: 암호화 도입 전 평문 oauth_id 계정 */
    Optional<User> findByOauthProviderAndOauthId(User.OAuthProvider provider, String oauthId);
}
