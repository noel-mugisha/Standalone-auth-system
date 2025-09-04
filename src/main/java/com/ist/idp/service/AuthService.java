package com.ist.idp.service;

import org.springframework.stereotype.Service;

import com.ist.idp.dto.RegisterRequest;
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
                .emailVerified(false) // Initially not verified
                .otp(otp)
                .otpGeneratedTime(LocalDateTime.now())
                .build();

        // 4. Save the user to the database
        User savedUser = userRepository.save(user);

        // 5. Send the verification email
        emailService.sendOtpEmail(user.getEmail(), otp);

        return savedUser;
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        // 1. Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if the provided OTP is correct
        if (!otp.equals(user.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // 3. Check if the OTP has expired
        LocalDateTime otpGeneratedTime = user.getOtpGeneratedTime();
        if (LocalDateTime.now().isAfter(otpGeneratedTime.plusMinutes(OTP_VALID_DURATION))) {
            throw new RuntimeException("OTP has expired");
        }

        // 4. Mark user as verified and clear OTP fields
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
}
