package com.ecommerce.niorra.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.ecommerce.niorra.entity.AppUser;
import com.ecommerce.niorra.service.AppUserService;
import com.ecommerce.niorra.service.JwtService;

class AuthControllerTest {

    private final AppUserService appUserService = mock(AppUserService.class);
    private final AuthController controller = new AuthController(mock(AuthenticationManager.class),
            mock(JwtService.class), appUserService);

    @Test
    void currentUserRejectsAnonymousAuthentication() {
        var anonymous = new AnonymousAuthenticationToken("key", "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        var response = controller.getCurrentUser(anonymous);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(appUserService);
    }

    @Test
    void currentUserLoadsPersistedGoogleProfileForJwtAuthentication() {
        String email = "customer@example.com";
        var userDetails = User.withUsername(email).password("").authorities("ROLE_CUSTOMER").build();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(userDetails, null,
                userDetails.getAuthorities());
        var appUser = AppUser.builder()
                .email(email)
                .name("Customer Name")
                .firstName("Customer")
                .lastName("Name")
                .provider("google")
                .profilePicture("https://example.com/profile.png")
                .build();
        when(appUserService.findByEmail(email)).thenReturn(Optional.of(appUser));

        var response = controller.getCurrentUser(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(email, response.getBody().get("email"));
        assertEquals(email, response.getBody().get("username"));
        assertEquals("google", response.getBody().get("provider"));
        assertEquals("Customer Name", response.getBody().get("name"));
        assertEquals("Customer", response.getBody().get("firstName"));
        assertEquals("Name", response.getBody().get("lastName"));
        assertEquals("https://example.com/profile.png", response.getBody().get("picture"));
    }
}
