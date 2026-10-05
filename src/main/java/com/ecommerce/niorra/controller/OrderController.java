package com.ecommerce.niorra.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.dto.CreateOrderRequest;
import com.ecommerce.niorra.dto.OrderResponse;
import com.ecommerce.niorra.dto.PayPalCheckoutResponse;
import com.ecommerce.niorra.enums.OrderStatus;
import com.ecommerce.niorra.service.OrderService;
import com.ecommerce.niorra.service.PayPalCheckoutService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PayPalCheckoutService payPalCheckoutService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request,
            Principal principal) {
        if (principal != null && (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank())) {
            request.setCustomerEmail(principal.getName());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @PostMapping("/{id}/paypal")
    public ResponseEntity<PayPalCheckoutResponse> createPayPalOrder(@PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(payPalCheckoutService.createPayPalOrder(id, authentication));
    }

    @PostMapping("/{id}/paypal/capture")
    public ResponseEntity<OrderResponse> capturePayPalOrder(@PathVariable Long id,
            Authentication authentication) {
        payPalCheckoutService.capturePayPalOrder(id, authentication);
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PostMapping("/{id}/paypal/cancel")
    public ResponseEntity<OrderResponse> cancelPayPalOrder(@PathVariable Long id,
            Authentication authentication) {
        payPalCheckoutService.cancelPayPalOrder(id, authentication);
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<List<OrderResponse>> getOrdersByCustomer(@PathVariable String email) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(email));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }
}
