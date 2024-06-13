package com.lazar;

import com.lazar.persistence.JDBIConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@Import({JDBIConfig.class})
public class LazarApplication {

	public static final boolean DEBUG_MODE = false;

	public static void main(String[] args) {
		SpringApplication.run(LazarApplication.class, args);
	}

	@PostConstruct
	public void init() {
		log.info("DEBUG MODE {}", DEBUG_MODE ? "ENABLED" : "DISABLED");
	}
	
}
