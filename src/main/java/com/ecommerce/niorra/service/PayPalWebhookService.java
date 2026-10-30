package com.ecommerce.niorra.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.niorra.config.PayPalProperties;
import com.ecommerce.niorra.entity.Order;
import com.ecommerce.niorra.entity.PayPalWebhookEvent;
import com.ecommerce.niorra.enums.PaymentStatus;
import com.ecommerce.niorra.repository.OrderRepository;
import com.ecommerce.niorra.repository.PayPalWebhookEventRepository;
import tools.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayPalWebhookService {

    private static final String CAPTURE_COMPLETED = "PAYMENT.CAPTURE.COMPLETED";

    private final PayPalService payPalService;
    private final PayPalProperties properties;
    private final OrderRepository orderRepository;
    private final PayPalWebhookEventRepository eventRepository;

    @Transactional
    public void handleWebhook(HttpHeaders headers, JsonNode event) {
        if (event == null || !event.isObject()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid PayPal webhook event");
        }

        var verification = payPalService.verifyWebhookSignature(headers, event);
        if (!"SUCCESS".equals(verification.get("verification_status"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "PayPal webhook signature is invalid");
        }

        String eventId = text(event, "id");
        String eventType = text(event, "event_type");
        if (eventId == null || eventType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PayPal webhook event is missing its ID or type");
        }
        if (eventRepository.existsById(eventId)) {
            return;
        }

        JsonNode resource = event.path("resource");
        String resourceId = text(resource, "id");
        PayPalWebhookEvent record = new PayPalWebhookEvent();
        record.setEventId(eventId);
        record.setEventType(eventType);
        record.setResourceId(resourceId);
        record.setReceivedAt(LocalDateTime.now());
        record.setProcessedAt(LocalDateTime.now());
        record.setProcessingResult(process(eventType, resource, resourceId));
        eventRepository.save(record);
    }

    private String process(String eventType, JsonNode resource, String captureId) {
        if (!CAPTURE_COMPLETED.equals(eventType)) {
            return "IGNORED_UNSUPPORTED_EVENT";
        }

        String paypalOrderId = text(resource.path("supplementary_data").path("related_ids"), "order_id");
        if (paypalOrderId == null || captureId == null || !"COMPLETED".equals(text(resource, "status"))) {
            return "INVALID_CAPTURE_DETAILS";
        }

        Order order = orderRepository.findByPaypalOrderIdForUpdate(paypalOrderId).orElse(null);
        if (order == null) {
            return "ORDER_NOT_FOUND";
        }
        if (order.getPaypalCaptureId() != null && !order.getPaypalCaptureId().equals(captureId)) {
            return "CAPTURE_ID_CONFLICT";
        }
        if (!amountMatches(order, resource.path("amount"))) {
            return "AMOUNT_OR_CURRENCY_MISMATCH";
        }
        if (order.getPaymentStatus() == PaymentStatus.REFUNDED) {
            return "ORDER_ALREADY_REFUNDED";
        }

        order.setPaypalCaptureId(captureId);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
        return order.getOrderStatus() != null && "CANCELLED".equals(order.getOrderStatus().name())
                ? "PAID_AFTER_CANCELLATION"
                : "PAID";
    }

    private boolean amountMatches(Order order, JsonNode amount) {
        String value = text(amount, "value");
        String currency = text(amount, "currency_code");
        if (value == null || currency == null || order.getTotalAmount() == null) {
            return false;
        }
        try {
            BigDecimal paidAmount = new BigDecimal(value);
            BigDecimal expectedAmount = BigDecimal.valueOf(order.getTotalAmount()).setScale(2, RoundingMode.HALF_UP);
            return properties.getCurrency().equals(currency) && expectedAmount.compareTo(paidAmount) == 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() && !value.textValue().isBlank() ? value.textValue() : null;
    }
}
