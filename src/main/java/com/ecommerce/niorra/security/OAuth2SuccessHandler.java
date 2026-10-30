package com.ecommerce.niorra.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AppUserService appUserService;
    private final JwtService jwtService;

    @Value("${app.frontend-url:https://niorra.eu}")
    // @Value("${app.frontend-url:http://localhost:5173}")
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

        String requestedRedirectUri = resolveRedirectUri(request);
        String redirectUrl = buildRedirectUrl(requestedRedirectUri, token, user, firstName, lastName, picture);
        response.sendRedirect(redirectUrl);
    }

    private String resolveRedirectUri(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
                return frontendUrl;
        }

        Object redirectUri = session.getAttribute("OAUTH2_REDIRECT_URI");
        session.removeAttribute("OAUTH2_REDIRECT_URI");

        return redirectUri instanceof String redirect && !redirect.isBlank() ? redirect : frontendUrl;
    }

    private String buildRedirectUrl(String requestedRedirectUri, String token, AppUser user,
            String firstName, String lastName, String picture) {
        String targetUrl = requestedRedirectUri == null || requestedRedirectUri.isBlank() ? frontendUrl
                : requestedRedirectUri;

        if (targetUrl.contains("#")) {
                int hashIndex = targetUrl.indexOf('#');
                String baseUrl = targetUrl.substring(0, hashIndex);
                String fragment = targetUrl.substring(hashIndex + 1);
                StringBuilder redirectBuilder = new StringBuilder(baseUrl).append("#");
                if (fragment != null && !fragment.isBlank()) {
                        redirectBuilder.append(fragment).append("?");
                } else {
                        redirectBuilder.append("?");
                }

                redirectBuilder.append("token=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
                redirectBuilder.append("&email=").append(URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8));
                if (firstName != null) {
                        redirectBuilder.append("&firstName=")
                                        .append(URLEncoder.encode(firstName, StandardCharsets.UTF_8));
                }
                if (lastName != null) {
                        redirectBuilder.append("&lastName=")
                                        .append(URLEncoder.encode(lastName, StandardCharsets.UTF_8));
                }
                if (picture != null) {
                        redirectBuilder.append("&picture=")
                                        .append(URLEncoder.encode(picture, StandardCharsets.UTF_8));
                }
                return redirectBuilder.toString();
        }

        if (targetUrl.startsWith("niorra://") || targetUrl.startsWith("myapp://")
                || targetUrl.startsWith("http://") || targetUrl.startsWith("https://")) {
                return UriComponentsBuilder.fromUriString(targetUrl)
                                .queryParam("token", token)
                                .queryParam("email", user.getEmail())
                                .queryParam("firstName", firstName == null ? "" : firstName)
                                .queryParam("lastName", lastName == null ? "" : lastName)
                                .queryParam("picture", picture == null ? "" : picture)
                                .build()
                                .encode()
                                .toUriString();
        }

        return UriComponentsBuilder.fromUriString(frontendUrl)
                        .pathSegment("google-success")
                        .queryParam("token", token)
                        .queryParam("email", user.getEmail())
                        .queryParam("firstName", firstName == null ? "" : firstName)
                        .queryParam("lastName", lastName == null ? "" : lastName)
                        .queryParam("picture", picture == null ? "" : picture)
                        .build()
                        .encode()
                        .toUriString();
    }
}
