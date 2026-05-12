package com.ecommerce.niorra.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.dto.ProductDTO;
import com.ecommerce.niorra.entity.Product;
import com.ecommerce.niorra.repository.ProductRepository;

@RestController
@RequestMapping("/admin/api/v1/products")
public class AdminController {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ModelMapper modelMapper = new ModelMapper();

	@GetMapping("/")
	public String home() {
		return "Welcome to Niorra E-commerce API!";
	}

	@PostMapping("/create")
	public ResponseEntity<?> addProduct(@RequestBody ProductDTO dto) {

		Product product = modelMapper.map(dto, Product.class);
		product.setProductImages(dto.getProductImages());
		product.setProductOccasion(dto.getProductOccasion());
		product.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		product.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		System.out.println(product);

		productRepository.save(product);

		return ResponseEntity.ok("Product Added Successfully!");
	}

}
