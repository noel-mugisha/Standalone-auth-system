package com.ist.idp.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
