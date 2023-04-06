package com.lazar;

import com.lazar.persistence.JDBIConfig;
import com.lazar.logging.LoggerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({JDBIConfig.class, LoggerConfig.class})
public class LazarApplication {

	public static final boolean DEBUG_MODE = false;

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(LazarApplication.class, args);
	}
	
}
