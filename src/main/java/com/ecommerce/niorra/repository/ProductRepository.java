package com.ecommerce.niorra.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.niorra.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}