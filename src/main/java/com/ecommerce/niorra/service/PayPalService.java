package com.ecommerce.niorra.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.niorra.config.PayPalProperties;
import com.ecommerce.niorra.entity.Order;

import tools.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayPalService {

        private final PayPalProperties properties;

        @SuppressWarnings("unchecked")
        public Map<String, Object> createOrder(Order order) {
                Map<String, Object> amount = Map.of(
                                "currency_code", properties.getCurrency(),
                                "value", String.format(java.util.Locale.ROOT, "%.2f", order.getTotalAmount()));
                Map<String, Object> purchaseUnit = Map.of(
                                "reference_id", order.getOrderNumber(),
                                "amount", amount);
                Map<String, Object> applicationContext = Map.of(
                                "return_url", properties.getReturnUrl(),
                                "cancel_url", properties.getCancelUrl(),
                                "user_action", "PAY_NOW");
                Map<String, Object> request = Map.of(
                                "intent", "CAPTURE",
                                "purchase_units", List.of(purchaseUnit),
                                "application_context", applicationContext);

                return client().post()
                                .uri("/v2/checkout/orders")
                                .headers(headers -> headers.setBearerAuth(accessToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(request)
                                .retrieve()
                                .body(Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> captureOrder(String paypalOrderId) {
                return client().post()
                                .uri("/v2/checkout/orders/{id}/capture", paypalOrderId)
                                .headers(headers -> headers.setBearerAuth(accessToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of())
                                .retrieve()
                                .body(Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> getOrderStatus(String paypalOrderId) {
                return client().get()
                                .uri("/v2/checkout/orders/{id}", paypalOrderId)
                                .headers(headers -> headers.setBearerAuth(accessToken()))
                                .retrieve()
                                .body(Map.class);
        }

        @SuppressWarnings("unchecked")
        public Map<String, Object> verifyWebhookSignature(HttpHeaders headers, JsonNode event) {
                if (properties.getWebhookId() == null || properties.getWebhookId().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                        "PayPal webhook ID is not configured");
                }

                Map<String, Object> request = Map.of(
                                "auth_algo", requiredHeader(headers, "PAYPAL-AUTH-ALGO"),
                                "cert_url", requiredHeader(headers, "PAYPAL-CERT-URL"),
                                "transmission_id", requiredHeader(headers, "PAYPAL-TRANSMISSION-ID"),
                                "transmission_sig", requiredHeader(headers, "PAYPAL-TRANSMISSION-SIG"),
                                "transmission_time", requiredHeader(headers, "PAYPAL-TRANSMISSION-TIME"),
                                "webhook_id", properties.getWebhookId(),
                                "webhook_event", event);

                return client().post()
                                .uri("/v1/notifications/verify-webhook-signature")
                                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(request)
                                .retrieve()
                                .body(Map.class);
        }

        private String requiredHeader(HttpHeaders headers, String name) {
                String value = headers.getFirst(name);
                if (value == null || value.isBlank()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                        "Missing PayPal webhook signature header: " + name);
                }
                return value;
        }

        private String accessToken() {
                if (properties.getClientId() == null || properties.getClientId().isBlank()
                                || properties.getClientSecret() == null || properties.getClientSecret().isBlank()) {
                        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                        "PayPal credentials are not configured");
                }

                MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
                form.add("grant_type", "client_credentials");
                Map<?, ?> response = client().post()
                                .uri("/v1/oauth2/token")
                                .headers(headers -> headers.setBasicAuth(properties.getClientId(),
                                                properties.getClientSecret()))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .body(form)
                                .retrieve()
                                .body(Map.class);
                return (String) response.get("access_token");
        }

        private RestClient client() {
                return RestClient.builder().baseUrl(properties.getBaseUrl()).build();
        }
}