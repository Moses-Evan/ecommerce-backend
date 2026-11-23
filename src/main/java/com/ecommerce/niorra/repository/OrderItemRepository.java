package com.ecommerce.niorra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.niorra.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}