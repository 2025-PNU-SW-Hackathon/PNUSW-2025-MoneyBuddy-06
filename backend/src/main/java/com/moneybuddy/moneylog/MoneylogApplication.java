package com.moneybuddy.moneylog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneylogApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneylogApplication.class, args);
	}

}
