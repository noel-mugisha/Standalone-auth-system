package com.ist.idp.service;

import com.ist.idp.exceptions.OtpException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;

    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject("Your Verification Code");
            message.setText("Your OTP for email verification is: " + otp + "\nIt is valid for 10 minutes.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new OtpException("Failed to send OTP email.");
        }
    }
}
