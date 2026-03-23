package com.imglog.myimagelogserver.auth;

import com.imglog.myimagelogserver.user.domain.User;
import com.imglog.myimagelogserver.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);

        String providerId = request.getClientRegistration().getRegistrationId();
        OAuth2Attributes attrs = OAuth2Attributes.of(providerId, oauth2User.getAttributes());

        User user = userRepository.findByOauthProviderAndOauthId(attrs.provider(), attrs.oauthId())
                .orElseGet(() -> userRepository.save(User.ofOAuth(
                        attrs.provider(), attrs.oauthId(), attrs.email(),
                        attrs.nickname(), attrs.profileImageUrl()
                )));

        return new AppOAuth2User(oauth2User, user.getId());
    }
}
