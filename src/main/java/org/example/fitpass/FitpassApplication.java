package org.example.fitpass;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class FitpassApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitpassApplication.class, args);
	}

}
