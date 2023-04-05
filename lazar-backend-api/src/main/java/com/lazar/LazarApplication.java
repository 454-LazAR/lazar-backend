package com.lazar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class LazarApplication {

	public static final boolean DEBUG_MODE = false;

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(LazarApplication.class, args);
	}
	
}
