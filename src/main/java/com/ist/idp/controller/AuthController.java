package com.ist.idp.controller;

import com.ist.idp.dto.request.LoginRequest;
import com.ist.idp.dto.request.RegisterRequest;
import com.ist.idp.dto.request.VerifyOtpRequest;
import com.ist.idp.dto.response.ApiMessageResponse;
import com.ist.idp.dto.response.AuthResponse;
import com.ist.idp.dto.response.AuthResponseDto;
import com.ist.idp.service.AuthService;
import com.ist.idp.utils.CookieUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return new ResponseEntity<>(new ApiMessageResponse("Registration successful. Please check your email for the verification OTP."), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        HttpHeaders headers = new HttpHeaders();
        cookieUtil.addCookieToResponseHeaders(headers, "refresh_token", authResponse.refreshToken());
        return ResponseEntity.ok()
                .headers(headers)
                .body(new AuthResponseDto(authResponse.accessToken()));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.ok(new ApiMessageResponse("Email verified successfully. You can now log in."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(
            @CookieValue(name = "refresh_token") String refreshToken
    ) {
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        HttpHeaders headers = new HttpHeaders();
        cookieUtil.addCookieToResponseHeaders(headers, "refresh_token", authResponse.refreshToken());
        return ResponseEntity.ok()
                .headers(headers)
                .body(new AuthResponseDto(authResponse.accessToken()));
    }

}
