package com.ecommerce.niorra.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ecommerce.niorra.config.PayPalProperties;
import com.ecommerce.niorra.dto.PayPalCheckoutResponse;
import com.ecommerce.niorra.entity.Order;
import com.ecommerce.niorra.entity.OrderItem;
import com.ecommerce.niorra.entity.Product;
import com.ecommerce.niorra.enums.OrderStatus;
import com.ecommerce.niorra.enums.PaymentMethod;
import com.ecommerce.niorra.enums.PaymentStatus;
import com.ecommerce.niorra.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayPalCheckoutService {

    private final OrderRepository orderRepository;
    private final PayPalService payPalService;
    private final PayPalProperties properties;

    @Transactional
    public PayPalCheckoutResponse createPayPalOrder(Long orderId, Authentication authentication) {
        Order order = findAuthorizedOrderForUpdate(orderId, authentication);
        if (order.getPaymentMethod() != PaymentMethod.PAYPAL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order payment method is not PayPal");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not awaiting payment");
        }
        if (order.getPaypalOrderId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PayPal checkout has already been started");
        }

        Map<String, Object> paypalOrder = payPalService.createOrder(order);
        String paypalOrderId = (String) paypalOrder.get("id");
        if (paypalOrderId == null || paypalOrderId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayPal did not return an order ID");
        }
        String approvalUrl = links(paypalOrder).stream()
                .filter(link -> "approve".equals(link.get("rel")) || "payer-action".equals(link.get("rel")))
                .map(link -> (String) link.get("href"))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        "PayPal did not return an approval link"));

        order.setPaypalOrderId(paypalOrderId);
        orderRepository.save(order);
        return new PayPalCheckoutResponse(paypalOrderId, approvalUrl);
    }

    @Transactional
    public void capturePayPalOrder(Long orderId, Authentication authentication) {
        Order order = findAuthorizedOrderForUpdate(orderId, authentication);
        if (order.getPaymentMethod() != PaymentMethod.PAYPAL || order.getPaypalOrderId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PayPal checkout has not been started");
        }
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order has been cancelled");
        }
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not awaiting payment");
        }

        Map<String, Object> capture = payPalService.captureOrder(order.getPaypalOrderId());
        if (!"COMPLETED".equals(capture.get("status"))) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PayPal payment was not completed");
        }

        Map<String, Object> amount = capturedAmount(capture);
        BigDecimal paidAmount = new BigDecimal((String) amount.get("value"));
        BigDecimal expectedAmount = BigDecimal.valueOf(order.getTotalAmount()).setScale(2, RoundingMode.HALF_UP);
        if (!properties.getCurrency().equals(amount.get("currency_code"))
                || expectedAmount.compareTo(paidAmount) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "PayPal capture amount or currency does not match the order");
        }

        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelPayPalOrder(Long orderId, Authentication authentication) {
        Order order = findAuthorizedOrderForUpdate(orderId, authentication);
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return;
        }
        if (order.getPaymentMethod() != PaymentMethod.PAYPAL || order.getPaypalOrderId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PayPal checkout has not been started");
        }
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only unpaid orders can be cancelled");
        }

        Map<String, Object> paypalOrder = payPalService.getOrderStatus(order.getPaypalOrderId());
        if ("COMPLETED".equals(paypalOrder.get("status"))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "PayPal reports this order as paid; it cannot be cancelled");
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            if (product != null) {
                int currentStock = java.util.Objects.requireNonNullElse(product.getProductStock(), 0);
                product.setProductStock(Math.addExact(currentStock, requiredQuantity(item)));
            }
        }
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private int requiredQuantity(OrderItem item) {
        Integer quantity = item.getQuantity();
        if (quantity == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Order item is missing its quantity; stock could not be restored");
        }
        return quantity;
    }

    private Order findAuthorizedOrderForUpdate(Long orderId, Authentication authentication) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin && !order.getCustomerEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to the current user");
        }
        return order;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> links(Map<String, Object> paypalOrder) {
        return (List<Map<String, Object>>) paypalOrder.getOrDefault("links", List.of());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> capturedAmount(Map<String, Object> capture) {
        try {
            List<Map<String, Object>> purchaseUnits = (List<Map<String, Object>>) capture.get("purchase_units");
            Map<String, Object> payments = (Map<String, Object>) purchaseUnits.get(0).get("payments");
            List<Map<String, Object>> captures = (List<Map<String, Object>>) payments.get("captures");
            return (Map<String, Object>) captures.get(0).get("amount");
        } catch (RuntimeException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "PayPal response did not contain capture details", exception);
        }
    }
}