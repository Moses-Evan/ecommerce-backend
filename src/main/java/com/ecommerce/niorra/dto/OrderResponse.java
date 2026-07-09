package com.ecommerce.niorra.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.ecommerce.niorr.enums.OrderStatus;
import com.ecommerce.niorr.enums.PaymentMethod;
import com.ecommerce.niorr.enums.PaymentStatus;

@Data
@Builder
public class OrderResponse {

    private Long id;

    private String orderNumber;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String shippingAddress;

    private String shippingCity;

    private String shippingState;

    private String shippingZip;

    private String shippingCountry;

    private Double subtotal;

    private Double discount;

    private Double shippingCharge;

    private Double tax;

    private Double totalAmount;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;

}