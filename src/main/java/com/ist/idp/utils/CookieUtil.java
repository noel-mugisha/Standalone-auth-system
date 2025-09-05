package com.ist.idp.utils;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpiration;

    public void addCookieToResponseHeaders(HttpHeaders headers, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) (refreshTokenExpiration / 1000))
                .sameSite("Lax")
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addCookieToServletResponse(HttpServletResponse response, String name, String value) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) (refreshTokenExpiration / 1000))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}