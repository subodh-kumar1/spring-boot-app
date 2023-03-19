package com.accountingapp.subodh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubodhApplication {

	public static void main(String[] args) {
		SpringApplication.run(SubodhApplication.class, args);
	}

}
