package com.ecommerce.niorra;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.admin.username=test-admin",
		"app.admin.password=test-password",
		"app.jwt.secret=test-secret-that-is-at-least-thirty-two-bytes-long"
})
class EcommerceBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
