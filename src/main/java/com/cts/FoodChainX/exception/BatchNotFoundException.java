package com.cts.foodchainx.exception;

/**
 * Exception thrown when a specific production batch cannot be located in the traceability system.
 * This typically occurs during a Consumer Portal search or a QR code scan if the 
 * provided Batch ID does not exist in the database or has no associated trace history.
 * * <p>Extends {@link RuntimeException} to allow for unchecked exception handling within 
 * the Spring Boot service layer.</p>
 */
public class BatchNotFoundException extends RuntimeException {

    /**
     * Constructs a new BatchNotFoundException with a formatted error message.
     *
     * @param batchId the unique identifier of the batch that was not found.
     */
    public BatchNotFoundException(Long batchId) {
        super("Trace history for Batch ID " + batchId + " not found.");
    }
}