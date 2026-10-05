package com.ecommerce.niorra.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.ecommerce.niorra.security.JwtAuthenticationFilter;
import com.ecommerce.niorra.security.OAuth2SuccessHandler;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class SecurityConfig {

        private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;

        public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                        OAuth2SuccessHandler oAuth2SuccessHandler) {
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
                this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        OAuth2AuthorizationRequestResolver authorizationRequestResolver) throws Exception {
                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**", "/api/products/**",
                                                                "/admin/api/v1/products/**")
                                                .permitAll()
                                                .requestMatchers("/api/orders/**", "/api/addresses/**").authenticated()
                                                .anyRequest().permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(endpoint -> endpoint
                                                                .authorizationRequestResolver(
                                                                                authorizationRequestResolver))
                                                .successHandler(oAuth2SuccessHandler)
                                                .failureHandler((request, response, exception) -> {
                                                        logger.error("Google OAuth2 login failed", exception);
                                                        response.sendRedirect("/login?error");
                                                }))
                                .logout(logout -> logout.logoutSuccessUrl("http://localhost:5173"))
                                .httpBasic(Customizer.withDefaults())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
                        ClientRegistrationRepository clientRegistrationRepository) {
                DefaultOAuth2AuthorizationRequestResolver defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                                clientRegistrationRepository, "/oauth2/authorization");

                return new OAuth2AuthorizationRequestResolver() {
                        @Override
                        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                                return addSelectAccountPrompt(authorizationRequest);
                        }

                        @Override
                        public OAuth2AuthorizationRequest resolve(HttpServletRequest request,
                                        String clientRegistrationId) {
                                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request,
                                                clientRegistrationId);
                                return addSelectAccountPrompt(authorizationRequest);
                        }

                        private OAuth2AuthorizationRequest addSelectAccountPrompt(
                                        OAuth2AuthorizationRequest authorizationRequest) {
                                if (authorizationRequest == null) {
                                        return null;
                                }

                                Map<String, Object> additionalParameters = new LinkedHashMap<>(
                                                authorizationRequest.getAdditionalParameters());
                                additionalParameters.put("prompt", "select_account");

                                return OAuth2AuthorizationRequest.from(authorizationRequest)
                                                .additionalParameters(additionalParameters)
                                                .build();
                        }
                };
        }

        @Bean
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
                UserDetails admin = User.withUsername("admin")
                                .password(passwordEncoder.encode("admin"))
                                .roles("ADMIN")
                                .build();

                UserDetails customer = User.withUsername("customer")
                                .password(passwordEncoder.encode("customer123"))
                                .roles("CUSTOMER")
                                .build();

                return new InMemoryUserDetailsManager(admin, customer);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
                return configuration.getAuthenticationManager();
        }
}