package com.ist.idp.config;

import com.ist.idp.repository.UserRepository;
import com.ist.idp.security.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value( "${frontend.redirect-url}")
    private String frontendRedirectUrl = "http://your-frontend-widget-s3-url.com/login-success";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication
    )
    throws IOException, ServletException, IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        // Find the user from our database (synced by CustomOAuth2UserService)
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found in database after OAuth2 login"));

        // Generate our custom JWTs
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Build the redirect URL with tokens as query parameters
        String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUrl)
                .queryParam("access_token", accessToken)
                .queryParam("refresh_token", refreshToken)
                .build().toUriString();

        // Redirect the user
        response.sendRedirect(redirectUrl);
    }
}
