package com.cts.foodchainx.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cts.foodchainx.dto.tracerecord.TraceRecordResponseDto;
import com.cts.foodchainx.exception.BatchNotFoundException;
import jakarta.persistence.EntityNotFoundException;

/**
 * Service class responsible for managing product traceability and transparency data.
 * It aggregates information from movement logs and quality inspections to provide 
 * a comprehensive "Farm-to-Table" view for consumers.
 */
@Service
public interface TraceabilityService {

    /**
     * Retrieves the most recent traceability state for a given batch.
     * Uses the latest movement record and attaches current quality certification status.
     *
     * @param batchId the unique ID of the production batch to trace
     * @return the most recent {@link TraceRecordResponseDto}
     * @throws BatchNotFoundException if no trace records exist for the specified batch
     */
    public TraceRecordResponseDto getTraceabilityData(Long batchId);

    /**
     * Generates a rich-text payload for QR code generation.
     * The format uses a piped string (FCX|...) containing critical batch info, 
     * harvest dates, and certification status for quick offline scanning.
     *
     * @param batchId the ID of the batch for which to generate the payload
     * @return a piped string containing batch transparency data
     * @throws EntityNotFoundException if the batch has no history to generate a payload from
     */
    public String generateQrPayload(Long batchId);

    /**
     * Retrieves the entire journey history of a specific batch.
     * Useful for building a timeline view in the consumer portal.
     *
     * @param batchId the production ID to look up
     * @return a chronological list of all traceability events
     */
    public List<TraceRecordResponseDto> getBatchHistory(Long batchId);
}