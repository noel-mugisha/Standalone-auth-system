package com.ist.idp.service;

import com.ist.idp.dto.response.AuthResponse;
import com.ist.idp.dto.request.LoginRequest;
import com.ist.idp.enums.Role;
import com.ist.idp.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import com.ist.idp.dto.request.RegisterRequest;
import com.ist.idp.exceptions.UserAlreadyExistsException;
import com.ist.idp.model.User;
import com.ist.idp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final long OTP_VALID_DURATION = 10; // 10 minutes

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists.");
        }
        String otp = generateOtp();
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .emailVerified(false) // Initially not verified
                .otp(otp)
                .otpGeneratedTime(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), otp);

        return savedUser;
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        // Check if the provided OTP is correct
        if (!otp.equals(user.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        // Check if the OTP has expired
        LocalDateTime otpGeneratedTime = user.getOtpGeneratedTime();
        if (LocalDateTime.now().isAfter(otpGeneratedTime.plusMinutes(OTP_VALID_DURATION))) {
            throw new RuntimeException("OTP has expired");
        }
        // Mark user as verified and clear OTP fields
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        // Generate a 6-digit OTP
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        return String.format("%06d", num);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.getSubjectFromToken(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponse(newAccessToken, newRefreshToken);
        }
        // Handle error correctly
        throw new RuntimeException("Refresh token is invalid");
    }
}
