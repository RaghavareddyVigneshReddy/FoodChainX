package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.tracerecord.TraceRecordResponseDto;
import com.cts.foodchainx.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Consumer Transparency Portal.
 * provides endpoints for traceability, QR code generation, and product history
 * specifically designed for end-consumer access.
 */

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@Slf4j
public class ConsumerPortalController {

    private final TraceabilityService traceabilityService;

    /**
     * Retrieves the most recent traceability data for a specific production batch.
     * This includes the current location, status, and quality certification details.
     *
     * @param batchId the unique identifier of the production batch
     * @return a {@link ResponseEntity} containing the {@link TraceRecordResponseDto}
     * @throws com.cts.foodchainx.exception.BatchNotFoundException if no record exists for the batchId
     */
    @GetMapping("/trace/{batchId}")
    public ResponseEntity<TraceRecordResponseDto> getTraceRecord(@PathVariable Long batchId) {
        log.info("REST request to get traceability data for Batch ID: {}", batchId);
        return ResponseEntity.ok(traceabilityService.getTraceabilityData(batchId));
    }

    /**
     * Generates a formatted string payload intended for QR code generation.
     * The payload contains a piped string (e.g., FCX|BatchID|Status|...) that can be
     * easily parsed by mobile scanners to display product details offline.
     *
     * @param batchId the unique identifier of the production batch
     * @return a {@link ResponseEntity} containing the raw QR payload string
     */
    @GetMapping("/qr/{batchId}")
    public ResponseEntity<String> getQrCodePayload(@PathVariable Long batchId) {
        log.info("REST request to generate QR payload for Batch ID: {}", batchId);
        return ResponseEntity.ok(traceabilityService.generateQrPayload(batchId));
    }

    /**
     * Retrieves the full journey history of a production batch.
     * Returns a chronological list of all movements (TraceRecords) from farm to current state.
     *
     * @param batchId the unique identifier of the production batch
     * @return a {@link ResponseEntity} containing a {@link List} of {@link TraceRecordResponseDto}
     */
    @GetMapping("/history/{batchId}")
    public ResponseEntity<List<TraceRecordResponseDto>> getBatchHistory(@PathVariable Long batchId) {
        log.info("REST request to get full history for Batch ID: {}", batchId);
        return ResponseEntity.ok(traceabilityService.getBatchHistory(batchId));
}
}