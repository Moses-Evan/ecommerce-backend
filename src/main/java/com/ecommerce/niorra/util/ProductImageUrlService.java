package com.ecommerce.niorra.util;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProductImageUrlService {
	private static final String PRODUCT_IMAGE_PATH = "/uploads/products/";

	private final String publicBaseUrl;
	private final String publicHost;

	public ProductImageUrlService(
			@Value("${app.public-base-url:https://api.niorra.eu}") String publicBaseUrl) {
		this.publicBaseUrl = publicBaseUrl.replaceAll("/+$", "");
		this.publicHost = URI.create(this.publicBaseUrl).getHost();
	}

	public List<String> toPublicUrls(List<String> imagePaths) {
		return imagePaths == null ? null : imagePaths.stream().map(this::toPublicUrl).toList();
	}

	public List<String> toStoredPaths(List<String> imageUrls) {
		return imageUrls == null ? null : imageUrls.stream().map(this::toStoredPath).toList();
	}

	public String toPublicUrl(String imagePath) {
		String relativePath = getManagedImagePath(imagePath);
		return relativePath == null ? imagePath : publicBaseUrl + relativePath;
	}

	public String toStoredPath(String imageUrl) {
		String relativePath = getManagedImagePath(imageUrl);
		return relativePath == null ? imageUrl : relativePath;
	}

	private String getManagedImagePath(String imageUrl) {
		if (imageUrl == null || imageUrl.isBlank()) {
			return null;
		}

		try {
			URI uri = URI.create(imageUrl);
			String path = uri.getRawPath();
			if (path == null) {
				return null;
			}

			if (path.startsWith("uploads/products/") && !uri.isAbsolute()) {
				path = "/" + path;
			}

			if (!path.startsWith(PRODUCT_IMAGE_PATH)) {
				return null;
			}

			if (!uri.isAbsolute() || isManagedHost(uri.getHost())) {
				return path;
			}
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Invalid product image URL", exception);
		}

		return null;
	}

	private boolean isManagedHost(String host) {
		return host != null && (host.equalsIgnoreCase(publicHost)
				|| host.equalsIgnoreCase("api.niorra.eu")
				|| host.equalsIgnoreCase("localhost")
				|| host.equals("127.0.0.1")
				|| host.equals("::1")
				|| host.equals("[::1]"));
	}
}
