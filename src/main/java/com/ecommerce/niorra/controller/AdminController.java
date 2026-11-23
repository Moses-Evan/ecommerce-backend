package com.ecommerce.niorra.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.niorra.dto.ProductDTO;
import com.ecommerce.niorra.entity.Product;
import com.ecommerce.niorra.repository.ProductRepository;
import com.ecommerce.niorra.util.ProductImageUrlService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/admin/api/v1/products")
public class AdminController {

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ModelMapper modelMapper = new ModelMapper();

	@Autowired
	private ProductImageUrlService productImageUrlService;

	@GetMapping("/")
	public String home() {
		return "Welcome to Niorra E-commerce API!";
	}

	@GetMapping
	public List<Product> getAllProducts() {
		return productRepository.findAll().stream()
				.map(this::withPublicImageUrls)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getProductById(@PathVariable Long id) {
		return productRepository.findById(id)
				.map(this::withPublicImageUrls)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping("/create")
	public ResponseEntity<?> addProduct(@RequestBody ProductDTO dto) {

		Product product = modelMapper.map(dto, Product.class);
		product.setProductImages(productImageUrlService.toStoredPaths(dto.getProductImages()));
		product.setProductOccasion(dto.getProductOccasion());
		product.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		product.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
		System.out.println(product);

		productRepository.save(product);

		return ResponseEntity.ok("Product Added Successfully!");
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
		Optional<Product> existingProduct = productRepository.findById(id);
		if (existingProduct.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
		}

		Product product = existingProduct.get();
		modelMapper.map(dto, product);
		product.setProductImages(productImageUrlService.toStoredPaths(dto.getProductImages()));
		product.setProductOccasion(dto.getProductOccasion());
		product.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

		return ResponseEntity.ok(withPublicImageUrls(productRepository.save(product)));
	}

	private Product withPublicImageUrls(Product product) {
		product.setProductImages(productImageUrlService.toPublicUrls(product.getProductImages()));
		return product;
	}

}
