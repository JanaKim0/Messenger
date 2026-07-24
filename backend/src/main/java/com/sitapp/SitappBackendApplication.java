package com.sitapp;

import java.util.Locale;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SitappBackendApplication {

	public static void main(String[] args) {
		// Keep validation messages, logs and formatting in English regardless of the OS locale.
		Locale.setDefault(Locale.ENGLISH);
		SpringApplication.run(SitappBackendApplication.class, args);
	}

}
