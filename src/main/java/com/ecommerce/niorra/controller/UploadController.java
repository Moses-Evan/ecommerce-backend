package com.ecommerce.niorra.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/products")
public class UploadController {

	private static final String UPLOAD_DIR = "uploads/products/";

	@PostMapping("/uploads")
	public ResponseEntity<List<String>> uploadProductImages(@RequestParam("images") List<MultipartFile> images)
			throws IOException {

		List<String> imageUrls = new ArrayList<>();

		Path uploadPath = Paths.get(UPLOAD_DIR);

		// create folder if not exists
		if (!Files.exists(uploadPath)) {

			Files.createDirectories(uploadPath);
		}

		for (MultipartFile file : images) {

			// generate unique filename
			String fileName = UUID.randomUUID() + ".webp";

			Path filePath = uploadPath.resolve(fileName);

			// save file
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// generate public url
			String imageUrl = "http://localhost:8080/uploads/products/" + fileName;

			imageUrls.add(imageUrl);
		}

		return ResponseEntity.ok(imageUrls);
	}
}