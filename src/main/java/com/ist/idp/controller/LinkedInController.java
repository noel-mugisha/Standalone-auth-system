package com.ist.idp.controller;

import com.ist.idp.dto.response.AuthResponse;
import com.ist.idp.enums.Role;
import com.ist.idp.model.User;
import com.ist.idp.repository.UserRepository;
import com.ist.idp.security.jwt.JwtService;
import com.ist.idp.service.LinkedInOAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/linkedin")
@RequiredArgsConstructor
public class LinkedInController {

    private final LinkedInOAuthService linkedInOAuthService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${linkedin.api.client-id}")
    private String clientId;
    @Value("${linkedin.api.redirect-uri}")
    private String redirectUri;
    @Value("${linkedin.api.scope}")
    private String scope;
    @Value("${linkedin.api.authorization-uri}")
    private String authorizationUri;
    @Value("${frontend.redirect-url}")
    private String frontendRedirectUrl;

    @GetMapping("/authorize")
    public void initiateAuthorization(HttpServletResponse response) throws IOException {
        String state = UUID.randomUUID().toString();

        String url = UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state)
                .queryParam("scope", scope)
                .toUriString();
        response.sendRedirect(url);
    }

    @GetMapping("/callback")
    public void handleCallback(@RequestParam("code") String code, @RequestParam("state") String state, HttpServletResponse httpServletResponse) throws IOException {
        String accessToken = linkedInOAuthService.getAccessToken(code);
        if (accessToken == null) {
            httpServletResponse.sendRedirect("/error?message=linkedin_token_exchange_failed");
            return;
        }

        LinkedInOAuthService.LinkedInUserDetails userDetails = linkedInOAuthService.getUserDetails(accessToken);
        User appUser = findOrCreateUser(userDetails);
        AuthResponse authResponse = new AuthResponse(
                jwtService.generateAccessToken(appUser),
                jwtService.generateRefreshToken(appUser)
        );

        String finalRedirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("access_token", authResponse.accessToken())
                .queryParam("refresh_token", authResponse.refreshToken())
                .build().toUriString();

        httpServletResponse.sendRedirect(finalRedirectUrl);
    }

    private User findOrCreateUser(LinkedInOAuthService.LinkedInUserDetails userDetails) {
        Optional<User> userOptional = userRepository.findByEmail(userDetails.email());

        return userOptional.map(existingUser -> {
            if (existingUser.getLinkedinId() == null) {
                existingUser.setLinkedinId(userDetails.sub());
                userRepository.save(existingUser);
            }
            return existingUser;
        }).orElseGet(() -> {
            User newUser = User.builder()
                    .email(userDetails.email())
                    .linkedinId(userDetails.sub())
                    .emailVerified(true)
                    .role(Role.USER)
                    .build();
            return userRepository.save(newUser);
        });
    }
}