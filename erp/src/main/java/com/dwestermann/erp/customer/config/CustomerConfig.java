package com.dwestermann.erp.customer.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = "com.dwestermann.erp.customer")
public class CustomerConfig {

    // Configuration for Customer module
    // Additional beans can be defined here if needed

}