package com.ecommerce.niorra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.niorra.entity.Address;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findAllByUserEmailOrderByIdDesc(String userEmail);
}