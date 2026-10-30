package com.ecommerce.niorra.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/api/v1/products")
public class UploadController {

	@Value("${app.upload-dir:uploads}")
	private String uploadDir;

	@Value("${app.public-base-url:http://localhost:8080}")
	private String publicBaseUrl;

	@PostMapping("/uploads")
	public ResponseEntity<List<String>> uploadProductImages(@RequestParam("images") List<MultipartFile> images)
			throws IOException {

		List<String> imageUrls = new ArrayList<>();
		Path uploadPath = Paths.get(uploadDir, "products").toAbsolutePath().normalize();

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		for (MultipartFile file : images) {
			String fileName = UUID.randomUUID() + getExtension(file.getOriginalFilename());
			Path filePath = uploadPath.resolve(fileName);
			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			String imageUrl = publicBaseUrl.replaceAll("/+$", "") + "/uploads/products/" + fileName;
			imageUrls.add(imageUrl);
		}

		return ResponseEntity.ok(imageUrls);
	}

	private String getExtension(String filename) {
		if (filename == null || filename.isBlank()) {
			return ".png";
		}
		int lastDot = filename.lastIndexOf('.');
		if (lastDot < 0 || lastDot == filename.length() - 1) {
			return ".png";
		}
		return filename.substring(lastDot);
	}
}