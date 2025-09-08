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

    // These values are now correctly loaded from the updated application.yml
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

        // The UriComponentsBuilder will correctly handle the space-separated scope.
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
        // Exchange authorization code for access token
        String accessToken = linkedInOAuthService.getAccessToken(code);

        if (accessToken == null) {
            // Handle error - redirect to a failure page
            httpServletResponse.sendRedirect("/error?message=linkedin_token_exchange_failed");
            return;
        }

        // Fetch user details from LinkedIn
        LinkedInOAuthService.LinkedInUserDetails userDetails = linkedInOAuthService.getUserDetails(accessToken);

        // Find or create user in the database (logic adapted from your CustomOAuth2UserService)
        User appUser = findOrCreateUser(userDetails);

        // Generate application-specific JWTs
        AuthResponse authResponse = new AuthResponse(
                jwtService.generateAccessToken(appUser),
                jwtService.generateRefreshToken(appUser)
        );

        // Redirect user to the frontend application with tokens
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