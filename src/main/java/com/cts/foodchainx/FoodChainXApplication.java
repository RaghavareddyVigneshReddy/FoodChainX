package com.cts.foodchainx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class FoodChainXApplication {

	public static void main(String[] args) {
		// Load .env variables into System properties
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(FoodChainXApplication.class, args);
		System.out.println("FoodChainX Application Started Successfully!");
	}

}
