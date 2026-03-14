package com.cts.foodchainx;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point for the FoodChainX Supply Chain Transparency System.
 * <p>
 * This class initializes the Spring context, enables background task scheduling,
 * and loads environmental configurations from a {@code .env} file.
 * </p>
 *
 * @author FoodChainX Development Team
 * @version 1.0
 */
@SpringBootApplication
@EnableScheduling
@Slf4j
public class FoodChainXApplication {

    /**
     * Main method that launches the Spring Boot application.
     * <p>
     * It configures environment variables via Dotenv before the Spring context
     * is initialized to ensure database credentials and secrets are available.
     * </p>
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        configureEnvironment();
        SpringApplication.run(FoodChainXApplication.class, args);
        log.info("FoodChainX Application Started Successfully!");
    }

    /**
     * Loads variables from the .env file into System properties.
     * Using a private method reduces clutter in the main method (SonarQube best practice).
     */
    private static void configureEnvironment() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> 
            System.setProperty(entry.getKey(), entry.getValue())
        );
        
        log.debug("Environment variables loaded into System properties.");
    }
}