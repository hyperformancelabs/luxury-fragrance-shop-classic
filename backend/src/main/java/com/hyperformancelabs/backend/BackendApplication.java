package com.hyperformancelabs.backend;

import com.hyperformancelabs.backend.util.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		EnvLoader.loadEnv(".env.local", ".env");

		SpringApplication.run(BackendApplication.class, args);
	}

}
