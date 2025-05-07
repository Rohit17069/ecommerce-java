package com.bootcamp.ecommerce_rohit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class EcommerceRohitApplication {
	public static void main(String[] args) {
		SpringApplication.run(EcommerceRohitApplication.class, args);
	}

}
