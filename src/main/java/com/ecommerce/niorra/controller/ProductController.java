package com.ecommerce.niorra.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.entity.Product;
import com.ecommerce.niorra.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
	private final ProductRepository productRepository;

	@GetMapping
	public List<Product> getAllProducts() {
		ObjectMapper mapper = new ObjectMapper();
		System.out.println("Fetching all products...");
		List<Product> products = productRepository.findAll();
		products.forEach(product -> {
		    List<String> updatedImages = product.getProductImages()
		            .stream()
		            .map(url -> url.replace(
		                    "http://localhost:8080",
		                    "https://ql4zl5fz-8080.inc1.devtunnels.ms"
		            ))
		            .toList();

		    product.setProductImages(updatedImages);
		});
		// System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(products));
		return products;
	}

	@GetMapping("/{id}")
	public Product getProductById(@PathVariable Long id) {
		Product product = productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
		product.setProductImages(
			    product.getProductImages().stream()
			        .map(url -> url.replace(
			            "http://localhost:8080",
			            "https://ql4zl5fz-8080.inc1.devtunnels.ms"
			        ))
			        .toList()
			);
		return product;
	}
}
