package com.ist.idp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    @Value("${frontend.login-widget}")
    private String frontendLoginWidget;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException, ServletException {

        // ======================= THE MOST IMPORTANT PART =======================
        // Log the exact exception to your server console. This will give us the root cause.
        logger.error("OAuth2 Login failed with error: {}", exception.getMessage());
        logger.error("Full exception stack trace:", exception);
        // =====================================================================

        // For the user, redirect them to a frontend page with a clear error message
        // This is better than showing a generic 401 page.
        String errorMessage = "linkedin_login_failed";

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendLoginWidget)
                .queryParam("error", "true")
                .queryParam("message", errorMessage)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}