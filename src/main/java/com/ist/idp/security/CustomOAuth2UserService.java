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
        // 1. Delegate to the default service to fetch the user details
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oauth2User.getAttributes();

        String email = (String) attributes.get("email");
        String linkedinId = (String) attributes.get("sub"); // 'sub' is the standard claim for subject identifier

        // 2. Find or create the user in our local database
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user = userOptional.map(existingUser -> {
            // Case 1: User with this email already exists. Link their LinkedIn ID if not already linked.
            if (existingUser.getLinkedinId() == null) {
                existingUser.setLinkedinId(linkedinId);
                userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            // Case 2: User is completely new. Create and save them.
            User newUser = User.builder()
                    .email(email)
                    .linkedinId(linkedinId)
                    .emailVerified(true) // We trust LinkedIn to have a verified email
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        });
        return oauth2User;
    }
}
