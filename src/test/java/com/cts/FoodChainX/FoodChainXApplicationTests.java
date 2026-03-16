package com.cts.foodchainx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
// This tells the test to look for your DB_USERNAME and DB_PASSWORD in the .env file
@ActiveProfiles("test")
class FoodChainXApplicationTests {

    @Test
    void contextLoads() {
        // If this passes, your Spring context and DB connection are healthy
    }
}