package com.example.realtime_message_application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RealtimemessageApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealtimemessageApplication.class, args);
	}

}
