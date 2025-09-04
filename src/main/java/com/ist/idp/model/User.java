package com.ist.idp.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(name = "email_verified")
    private boolean emailVerified = false;

    @Column(name = "linkedin_id", unique = true)
    private String linkedinId;

    private String otp;

    @Column(name = "otp_generated_time")
    private LocalDateTime otpGeneratedTime;

    // --- UserDetails Methods ---
    // These methods are required by Spring Security. For now, we'll use simple implementations.
    // We will enhance roles/authorities in the "Bonus" section later.

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now, we are not implementing roles. Return an empty list.
        return Collections.emptyList();
    }

    @Override
    public String getUsername() {
        // In our system, the email is the username.
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // For now, accounts never expire.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // For now, accounts are never locked.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // For now, credentials never expire.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // An account is enabled only if the email has been verified.
        return emailVerified;
    }
}