package com.ecommerce.niorra.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.dto.AuthResponse;
import com.ecommerce.niorra.dto.LoginRequest;
import com.ecommerce.niorra.service.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority())
                .orElse("ROLE_USER");

        String token = jwtService.generateToken(loginRequest.getUsername(), role);

        return ResponseEntity.ok(new AuthResponse(token, loginRequest.getUsername(), role, "Login successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Object principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Map<String, Object> user = new HashMap<>();
        user.put("provider", "local");

        if (principal instanceof OAuth2User oauth2User) {
            user.put("provider", "google");
            user.put("email", oauth2User.getAttribute("email"));
            user.put("name", oauth2User.getAttribute("name"));
            user.put("picture", oauth2User.getAttribute("picture"));
            user.put("username", oauth2User.getAttribute("email"));
            return ResponseEntity.ok(user);
        }

        user.put("username", principal.toString());
        user.put("roles", SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/google")
    public ResponseEntity<Map<String, String>> googleLogin() {
        return ResponseEntity.ok(Map.of("redirectUrl", "/oauth2/authorization/google"));
    }
}
