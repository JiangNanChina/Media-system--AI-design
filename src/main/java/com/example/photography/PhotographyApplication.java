package com.example.photography;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PhotographyApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotographyApplication.class, args);
	}

}
