package com.ecommerce.niorra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.niorr.enums.OrderStatus;
import com.ecommerce.niorra.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    List<Order> findByOrderStatus(OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

}