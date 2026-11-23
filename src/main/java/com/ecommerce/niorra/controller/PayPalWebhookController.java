package com.ecommerce.niorra.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.service.PayPalWebhookService;
import tools.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/paypal/webhooks")
@RequiredArgsConstructor
public class PayPalWebhookController {

    private final PayPalWebhookService webhookService;

    @PostMapping
    public ResponseEntity<Void> receiveWebhook(@RequestHeader HttpHeaders headers, @RequestBody JsonNode event) {
        webhookService.handleWebhook(headers, event);
        return ResponseEntity.noContent().build();
    }
}
