package com.ecommerce.niorra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "paypal")
public class PayPalProperties {

    private String baseUrl = "https://api-m.sandbox.paypal.com";
    private String clientId;
    private String clientSecret;
    private String currency = "USD";
    private String returnUrl;
    private String cancelUrl;
}