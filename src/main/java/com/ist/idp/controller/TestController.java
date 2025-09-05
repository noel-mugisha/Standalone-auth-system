package com.ist.idp.controller;

import com.ist.idp.dto.response.ApiMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity<?> sayHello(Principal principal) {
        return ResponseEntity.ok(new ApiMessageResponse("Hello, " + principal.getName()));
    }
}