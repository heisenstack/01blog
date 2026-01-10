package com.zerooneblog.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZerooneblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZerooneblogApplication.class, args);
		System.out.println("Server is running! Access it at: http://localhost:8080");
	}
}
