package com.ist.idp.controller;

import com.ist.idp.dto.response.ApiMessageResponse;
import com.ist.idp.enums.Role;
import com.ist.idp.exceptions.ResourceNotFoundException;
import com.ist.idp.model.User;
import com.ist.idp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;

    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> payload) {
        String roleString = payload.get("role");
        if (roleString == null) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse("Role is required."));
        }

        Role newRole;
        try {
            newRole = Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiMessageResponse("Invalid role provided."));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok(new ApiMessageResponse("User role updated successfully to " + newRole.name()));
    }
}