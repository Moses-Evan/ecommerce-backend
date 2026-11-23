package com.ecommerce.niorra.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class ProductImageUrlServiceTest {
	private final ProductImageUrlService service = new ProductImageUrlService("https://api.niorra.eu/");

	@Test
	void convertsRelativeProductImagePathsToPublicUrls() {
		assertEquals(
				"https://api.niorra.eu/uploads/products/item.webp",
				service.toPublicUrl("/uploads/products/item.webp"));
	}

	@Test
	void storesManagedAbsoluteUrlsAsRelativePaths() {
		assertEquals(
				List.of("/uploads/products/item.webp"),
				service.toStoredPaths(List.of("https://api.niorra.eu/uploads/products/item.webp")));
		assertEquals(
				"/uploads/products/legacy.webp",
				service.toStoredPath("http://localhost:8080/uploads/products/legacy.webp"));
	}

	@Test
	void preservesExternalImageUrls() {
		assertEquals(
				"https://cdn.example.com/products/item.webp",
				service.toStoredPath("https://cdn.example.com/products/item.webp"));
	}

	@Test
	void rejectsMalformedImageUrls() {
		assertThrows(
				IllegalArgumentException.class,
				() -> service.toStoredPath("https:// api.niorra.eu/uploads/products/item.webp"));
	}
}
