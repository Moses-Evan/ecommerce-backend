package com.ecommerce.niorra.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ecommerce.niorra.entity.AppUser;
import com.ecommerce.niorra.service.AppUserService;
import com.ecommerce.niorra.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserService appUserService;
    private final JwtService jwtService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String picture = oAuth2User.getAttribute("picture");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        String locale = oAuth2User.getAttribute("locale");
        String providerUserId = oAuth2User.getAttribute("sub");

        if (firstName == null || lastName == null) {
            String[] nameParts = name == null ? new String[0] : name.trim().split("\\s+", 2);
            if (firstName == null && nameParts.length > 0) {
                firstName = nameParts[0];
            }
            if (lastName == null && nameParts.length > 1) {
                lastName = nameParts[1];
            }
        }

        AppUser user = appUserService.findOrCreateGoogleUser(email, name, firstName, lastName,
                providerUserId, picture, emailVerified, locale);
        String token = jwtService.generateToken(user.getEmail(), "ROLE_CUSTOMER");

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .pathSegment("google-success")
                .queryParam("token", token)
                .queryParam("email", user.getEmail())
                .queryParam("firstName", firstName == null ? "" : firstName)
                .queryParam("lastName", lastName == null ? "" : lastName)
                .queryParam("picture", picture == null ? "" : picture)
                .build()
                .encode()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
