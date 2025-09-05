package com.ist.idp.security;

import com.ist.idp.enums.Role;
import com.ist.idp.model.User;
import com.ist.idp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();
        String email = (String) attributes.get("email");
        String linkedinId = (String) attributes.get("sub");
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user = userOptional.map(existingUser -> {
            if (existingUser.getLinkedinId() == null) {
                existingUser.setLinkedinId(linkedinId);
                userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .linkedinId(linkedinId)
                    .emailVerified(true)
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        });
        return oauth2User;
    }
}
