package com.ecommerce.niorra.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.dto.AuthResponse;
import com.ecommerce.niorra.dto.LoginRequest;
import com.ecommerce.niorra.entity.AppUser;
import com.ecommerce.niorra.service.AppUserService;
import com.ecommerce.niorra.service.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AppUserService appUserService;

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
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not authenticated"));
        }

        Object principal = authentication.getPrincipal();
        OAuth2User oauth2User = principal instanceof OAuth2User user ? user : null;
        String email = oauth2User != null
                ? oauth2User.getAttribute("email")
                : principal instanceof UserDetails userDetails ? userDetails.getUsername() : authentication.getName();
        AppUser appUser = email == null ? null : appUserService.findByEmail(email).orElse(null);

        Map<String, Object> user = new HashMap<>();
        user.put("provider", appUser != null ? appUser.getProvider() : oauth2User != null ? "google" : "local");
        user.put("email", email);
        user.put("username", email);
        user.put("name", appUser != null ? appUser.getName()
                : oauth2User != null ? oauth2User.getAttribute("name") : null);
        user.put("firstName", appUser != null ? appUser.getFirstName()
                : oauth2User != null ? oauth2User.getAttribute("given_name") : null);
        user.put("lastName", appUser != null ? appUser.getLastName()
                : oauth2User != null ? oauth2User.getAttribute("family_name") : null);
        user.put("picture", appUser != null ? appUser.getProfilePicture()
                : oauth2User != null ? oauth2User.getAttribute("picture") : null);
        user.put("roles", authentication.getAuthorities());
        return ResponseEntity.ok(user);
    }

    @GetMapping("/google")
    public ResponseEntity<Map<String, String>> googleLogin() {
        return ResponseEntity.ok(Map.of("redirectUrl", "/oauth2/authorization/google"));
    }
}
