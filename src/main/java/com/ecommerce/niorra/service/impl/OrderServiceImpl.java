package com.ecommerce.niorra.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.niorra.dto.CreateOrderRequest;
import com.ecommerce.niorra.dto.OrderItemRequest;
import com.ecommerce.niorra.dto.OrderItemResponse;
import com.ecommerce.niorra.dto.OrderResponse;
import com.ecommerce.niorra.entity.Order;
import com.ecommerce.niorra.entity.OrderItem;
import com.ecommerce.niorra.entity.Product;
import com.ecommerce.niorra.enums.OrderStatus;
import com.ecommerce.niorra.enums.PaymentMethod;
import com.ecommerce.niorra.enums.PaymentStatus;
import com.ecommerce.niorra.repository.OrderRepository;
import com.ecommerce.niorra.repository.ProductRepository;
import com.ecommerce.niorra.service.OrderService;
import com.ecommerce.niorra.util.OrderNumberGenerator;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZip(request.getShippingZip());
        order.setShippingCountry(request.getShippingCountry());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : PaymentMethod.PAYPAL);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        double subtotal = 0.0;
        double discountAmount = 0.0;
        double shippingCharge = 0.0;

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            if (product.getProductStock() == null || product.getProductStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getProductName());
            }

            double price = product.getProductSellingPrice() != null ? product.getProductSellingPrice()
                    : product.getProductMrp();
            double itemSubtotal = price * itemRequest.getQuantity();
            subtotal += itemSubtotal;
            if (product.getProductDiscount() != null) {
                discountAmount += itemSubtotal * product.getProductDiscount() / 100.0;
            }

            product.setProductStock(product.getProductStock() - itemRequest.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getProductName())
                    .productImage(getFirstImage(product))
                    .productSku(product.getProductSku())
                    .productPrice(price)
                    .quantity(itemRequest.getQuantity())
                    .subtotal(itemSubtotal)
                    .build();

            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        double discount = subtotal == 0.0 ? 0.0 : discountAmount / subtotal * 100.0;
        double tax = 0.0;
        double totalAmount = subtotal + shippingCharge;

        order.setSubtotal(subtotal);
        order.setDiscount(discount);
        order.setShippingCharge(shippingCharge);
        order.setTax(tax);
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        return toResponse(order);
    }

    @Override
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with order number: " + orderNumber));
        return toResponse(order);
    }

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByCustomer(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByOrderStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        order.setOrderStatus(status);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        order.setPaymentStatus(paymentStatus);
        order.setUpdatedAt(java.time.LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        orderRepository.delete(order);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomerName())
                .customerEmail(order.getCustomerEmail())
                .customerPhone(order.getCustomerPhone())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingState(order.getShippingState())
                .shippingZip(order.getShippingZip())
                .shippingCountry(order.getShippingCountry())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .shippingCharge(order.getShippingCharge())
                .tax(order.getTax())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .items(order.getOrderItems() == null ? List.of()
                        : order.getOrderItems().stream()
                                .map(item -> OrderItemResponse.builder()
                                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                                        .productName(item.getProductName())
                                        .productImage(item.getProductImage())
                                        .price(item.getProductPrice())
                                        .quantity(item.getQuantity())
                                        .subtotal(item.getSubtotal())
                                        .build())
                                .collect(Collectors.toList()))
                .build();
    }

    private String getFirstImage(Product product) {
        if (product.getProductImages() == null || product.getProductImages().isEmpty()) {
            return null;
        }
        return product.getProductImages().get(0);
    }
}
