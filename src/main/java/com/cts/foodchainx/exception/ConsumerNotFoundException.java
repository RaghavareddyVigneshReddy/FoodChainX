package com.cts.foodchainx.exception;

public class ConsumerNotFoundException extends RuntimeException {
    public ConsumerNotFoundException(String message) {
        super(message);
    }
}
