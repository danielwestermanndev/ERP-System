package com.dwestermann.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.dwestermann.erp" // ✅ Scannt alle Packages unter erp
})
@EnableJpaRepositories(basePackages = {
		"com.dwestermann.erp.**.repository" // ✅ Explizite Repository-Scanning
})
public class ErpSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(ErpSystemApplication.class, args);
	}
}
