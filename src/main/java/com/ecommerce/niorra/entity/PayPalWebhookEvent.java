package com.ecommerce.niorra.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paypal_webhook_events")
@Getter
@Setter
@NoArgsConstructor
public class PayPalWebhookEvent {

    @Id
    @Column(length = 100)
    private String eventId;

    @Column(nullable = false)
    private String eventType;

    private String resourceId;

    @Column(nullable = false)
    private String processingResult;

    @Column(nullable = false)
    private LocalDateTime receivedAt;

    @Column(nullable = false)
    private LocalDateTime processedAt;
}
