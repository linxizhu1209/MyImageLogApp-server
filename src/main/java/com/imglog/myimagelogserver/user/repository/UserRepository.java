package com.imglog.myimagelogserver.user.repository;

import com.imglog.myimagelogserver.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthProviderAndOauthId(User.OAuthProvider provider, String oauthId);
}
