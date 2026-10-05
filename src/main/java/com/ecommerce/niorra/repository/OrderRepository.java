package com.ecommerce.niorra.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.niorra.entity.Order;
import com.ecommerce.niorra.enums.OrderStatus;

import jakarta.persistence.LockModeType;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    List<Order> findByOrderStatus(OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

}