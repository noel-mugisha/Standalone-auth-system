package com.ist.idp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponseDto (
        @JsonProperty("access_token")
        String accessToken
) {}
