package com.ist.idp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/test")
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity<String> sayHello(Principal principal) {
        // Principal is automatically populated by Spring Security if the user is authenticated
        return ResponseEntity.ok("Hello, " + principal.getName());
    }
}