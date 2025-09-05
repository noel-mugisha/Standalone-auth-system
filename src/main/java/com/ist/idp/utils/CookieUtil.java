package com.ist.idp.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    @Value("${jwt.refresh-token.expiration-ms}")
    private long refreshTokenExpiration;

    public static ResponseCookie createRefreshTokenCookie(String refreshToken, long duration) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(duration)
                .sameSite("Strict")
                .build();
    }

    public void addCookieToResponse(
            HttpHeaders headers, String name, String value
    ) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge((int) (refreshTokenExpiration / 1000)) // Cookie age in seconds
                .sameSite("Lax")
                .build();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}