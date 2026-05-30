package com.imglog.myimagelogserver.auth;

import com.imglog.myimagelogserver.security.crypto.EncryptionService;
import com.imglog.myimagelogserver.user.domain.User;
import com.imglog.myimagelogserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);

        String providerId = request.getClientRegistration().getRegistrationId();
        OAuth2Attributes attrs = OAuth2Attributes.of(providerId, oauth2User.getAttributes());
        String lookupKey = encryptionService.oauthLookupKey(attrs.provider().name(), attrs.oauthId());

        User user = userRepository.findByOauthProviderAndOauthLookupKey(attrs.provider(), lookupKey)
                .or(() -> userRepository.findByOauthProviderAndOauthId(attrs.provider(), attrs.oauthId()))
                .map(existing -> migrateIfNeeded(existing, lookupKey))
                .orElseGet(() -> userRepository.save(User.ofOAuth(
                        attrs.provider(),
                        lookupKey,
                        attrs.oauthId(),
                        attrs.email(),
                        attrs.nickname(),
                        attrs.profileImageUrl()
                )));

        return new AppOAuth2User(oauth2User, user.getId());
    }

    private User migrateIfNeeded(User user, String lookupKey) {
        if (user.getOauthLookupKey() == null || !lookupKey.equals(user.getOauthLookupKey())) {
            user.migrateOauthLookupKey(lookupKey);
        }
        return user;
    }
}
