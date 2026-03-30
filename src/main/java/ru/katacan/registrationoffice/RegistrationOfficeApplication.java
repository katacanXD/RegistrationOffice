package ru.katacan.registrationoffice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RegistrationOfficeApplication {

	public static void main(String[] args) {
		SpringApplication.run(RegistrationOfficeApplication.class, args);
	}
}
