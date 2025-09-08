package com.ist.idp.service;

import com.ist.idp.dto.request.LoginRequest;
import com.ist.idp.dto.request.RegisterRequest;
import com.ist.idp.dto.response.AuthResponse;
import com.ist.idp.enums.Role;
import com.ist.idp.exceptions.OtpException;
import com.ist.idp.exceptions.ResourceNotFoundException;
import com.ist.idp.exceptions.TokenRefreshException;
import com.ist.idp.exceptions.UserAlreadyExistsException;
import com.ist.idp.model.User;
import com.ist.idp.repository.UserRepository;
import com.ist.idp.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long OTP_VALID_DURATION = 10; // 10 minutes
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + request.email() + " already exists.");
        }
        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException("Cannot register as an ADMIN.");
        }
        String otp = generateOtp();
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (!otp.equals(user.getOtp())) {
            throw new OtpException("Invalid OTP provided.");
        }
        LocalDateTime otpGeneratedTime = user.getOtpGeneratedTime();
        if (LocalDateTime.now().isAfter(otpGeneratedTime.plusMinutes(OTP_VALID_DURATION))) {
            throw new OtpException("OTP has expired. Please register again to receive a new one.");
        }
        user.setEmailVerified(true);
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(1000000);
        return String.format("%06d", num);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.getSubjectFromToken(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for the provided token."));
            String newAccessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponse(newAccessToken, newRefreshToken);
        }
        // Handle error correctly
        throw new TokenRefreshException("Refresh token is invalid");
    }
}
