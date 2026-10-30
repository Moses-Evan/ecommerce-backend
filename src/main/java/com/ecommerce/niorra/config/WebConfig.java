package com.ecommerce.niorra.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Value("${app.upload-dir:uploads}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		String uploadLocation = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(uploadLocation);
	}
}