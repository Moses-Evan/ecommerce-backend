package com.ecommerce.niorra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.niorra.entity.PayPalWebhookEvent;

public interface PayPalWebhookEventRepository extends JpaRepository<PayPalWebhookEvent, String> {
}
