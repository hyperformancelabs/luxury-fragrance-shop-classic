package com.hyperformancelabs.backend;

import com.hyperformancelabs.backend.util.EnvLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

	@BeforeAll
	static void setup() {
		// Load environment variables before tests run
		EnvLoader.loadEnv(".env.local", ".env");
	}

	@Test
	void contextLoads() {
	}

}
