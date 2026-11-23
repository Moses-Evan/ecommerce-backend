package com.ecommerce.niorra.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.entity.Product;
import com.ecommerce.niorra.repository.ProductRepository;
import com.ecommerce.niorra.util.ProductImageUrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
	private final ProductRepository productRepository;
	private final ProductImageUrlService productImageUrlService;

	@GetMapping
	public List<Product> getAllProducts() {
		System.out.println("Fetching all products...");
		List<Product> products = productRepository.findAll();
		products.forEach(this::addPublicImageUrls);
		return products;
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
		Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
		return addPublicImageUrls(product);
	}

	private Product addPublicImageUrls(Product product) {
		product.setProductImages(productImageUrlService.toPublicUrls(product.getProductImages()));
		return product;
	}
}
