package com.dwestermann.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {
		"com.dwestermann.erp.security.repository",
		"com.dwestermann.erp.customer.repository"
})
public class ErpSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(ErpSystemApplication.class, args);
	}
}
