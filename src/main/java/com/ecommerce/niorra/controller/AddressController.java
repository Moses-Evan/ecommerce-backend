package com.ecommerce.niorra.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.dto.AddressRequest;
import com.ecommerce.niorra.dto.AddressResponse;
import com.ecommerce.niorra.service.AddressService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressResponse> saveAddress(@Valid @RequestBody AddressRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.saveAddress(request, getUserEmail(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(Authentication authentication) {
        return ResponseEntity.ok(addressService.getAddresses(getUserEmail(authentication)));
    }

    private String getUserEmail(Authentication authentication) {
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }
        return authentication.getName();
    }
}