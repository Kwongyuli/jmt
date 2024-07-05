package com.example.jmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class JmtApplication {
	public static void main(String[] args) {
		SpringApplication.run(JmtApplication.class, args);
	}

}
