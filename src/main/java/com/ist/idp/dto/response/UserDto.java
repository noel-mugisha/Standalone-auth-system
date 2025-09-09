package com.ist.idp.dto.response;

import java.time.LocalDateTime;

public record UserDto (
        Long id,
        String email,
        String role,
        boolean emailVerified,
        LocalDateTime createdAt
) {}