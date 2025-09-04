package com.ist.idp.controller;

import com.ist.idp.dto.AuthResponse;
import com.ist.idp.dto.LoginRequest;
import com.ist.idp.dto.RegisterRequest;
import com.ist.idp.dto.VerifyOtpRequest;
import com.ist.idp.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Marks this class as a REST controller
@RequestMapping("/api/auth") // Base URL for all endpoints in this controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return new ResponseEntity<>("Registration successful. Please check your email for the verification OTP.", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.email(), request.otp());
        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

}
