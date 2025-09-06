package com.ist.idp.dto.request;

import com.ist.idp.enums.Role;

public record RegisterRequest(String email, String password, Role role) {
}
