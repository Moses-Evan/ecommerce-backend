package com.ecommerce.niorra.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ecommerce.niorra.entity.AppUser;
import com.ecommerce.niorra.repository.AppUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUser findOrCreateGoogleUser(String email, String name, String firstName, String lastName,
            String providerUserId, String profilePicture, Boolean emailVerified, String locale) {
        LocalDateTime loginTime = LocalDateTime.now();
        Optional<AppUser> existingUser = appUserRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();
            user.setName(name);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setProfilePicture(profilePicture);
            user.setEmailVerified(emailVerified);
            user.setLocale(locale);
            user.setLastLoginAt(loginTime);
            if (user.getProvider() == null || user.getProvider().isBlank()) {
                user.setProvider("google");
            }
            if (user.getProviderUserId() == null || user.getProviderUserId().isBlank()) {
                user.setProviderUserId(providerUserId);
            }
            return appUserRepository.save(user);
        }

        AppUser user = AppUser.builder()
                .name(name)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .provider("google")
                .providerUserId(providerUserId)
                .profilePicture(profilePicture)
                .emailVerified(emailVerified)
                .locale(locale)
                .lastLoginAt(loginTime)
                .password(null)
                .build();

        return appUserRepository.save(user);
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }
}
