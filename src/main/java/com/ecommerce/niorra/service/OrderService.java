package com.ecommerce.niorra.service;

import java.util.List;

import com.ecommerce.niorra.dto.CreateOrderRequest;
import com.ecommerce.niorra.dto.OrderResponse;
import com.ecommerce.niorra.enums.OrderStatus;
import com.ecommerce.niorra.enums.PaymentStatus;

public interface OrderService {

	OrderResponse createOrder(CreateOrderRequest request);

	OrderResponse getOrder(Long id);

	OrderResponse getOrderByOrderNumber(String orderNumber);

	List<OrderResponse> getAllOrders();

	List<OrderResponse> getOrdersByCustomer(String email);

	List<OrderResponse> getOrdersByStatus(OrderStatus status);

	OrderResponse updateOrderStatus(Long orderId, OrderStatus status);

	OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus);

	void deleteOrder(Long orderId);

}