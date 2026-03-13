package com.cts.foodchainx.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.foodchainx.dto.batch.BatchDetailResponseDto;
import com.cts.foodchainx.dto.batch.BatchRequestDto;
import com.cts.foodchainx.dto.batch.BatchResponseDto;
import com.cts.foodchainx.service.ProductionBatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for managing Production Batches in the food supply chain.
 * Handles the lifecycle of a batch from harvest creation to tracking and deletion.
 */
@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@Slf4j
public class ProductionBatchController {

    private final ProductionBatchService service;
    private final ProductionBatchService batchService;

    /**
     * Creates a new production batch for a specific farm.
     * <p><b>Endpoint:</b> POST /api/production/add</p>
     * * @param dto The request object containing farm ID, crop type, quantity, and harvest date.
     * @return ResponseEntity containing the created BatchResponseDto and HTTP status 201 CREATED.
     */
    @PostMapping("/add")
    public ResponseEntity<BatchResponseDto> createBatch(@Valid @RequestBody BatchRequestDto dto) {
        log.info("REST request to create new Production Batch for Farm ID: {}", dto.getFarmId());
        return new ResponseEntity<>(service.createBatch(dto), HttpStatus.CREATED);
    }

    /**
     * Retrieves basic information for a specific production batch by its ID.
     * <p><b>Endpoint:</b> GET /api/production/{id}</p>
     * * @param id The unique identifier of the production batch.
     * @return ResponseEntity containing the BatchResponseDto and HTTP status 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponseDto> getBatchById(@PathVariable @NonNull Long id) {
        return ResponseEntity.ok(service.getBatchById(id));
    }

    /**
     * Retrieves a list of all production batches associated with a specific farm.
     * <p><b>Endpoint:</b> GET /api/production/farm/{farmId}</p>
     * * @param farmId The ID of the farm whose batches are to be retrieved.
     * @return ResponseEntity containing a list of BatchResponseDto objects and HTTP status 200 OK.
     */
    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<BatchResponseDto>> getBatchesByFarm(@PathVariable @NonNull Long farmId) {
        return ResponseEntity.ok(service.getBatchesByFarm(farmId));
    }

    /**
     * Deletes a production batch from the system. 
     * Note: Typically involves business logic checks (e.g., cannot delete if already certified).
     * <p><b>Endpoint:</b> DELETE /api/production/{id}</p>
     * * @param id The unique identifier of the batch to be deleted.
     * @return ResponseEntity containing a confirmation message and HTTP status 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBatch(@PathVariable @NonNull Long id) {
        log.warn("REST request to DELETE Production Batch ID: {}", id);
        return ResponseEntity.ok(service.deleteBatch(id));
    }

    /**
     * Retrieves comprehensive details of a batch, including farm info and quality check findings.
     * <p><b>Endpoint:</b> GET /api/production/{batchId}/details</p>
     * * @param batchId The unique identifier of the production batch.
     * @return ResponseEntity containing BatchDetailResponseDto and HTTP status 200 OK.
     */
    @GetMapping("/{batchId}/details")
    public ResponseEntity<BatchDetailResponseDto> getBatchFullDetails(@PathVariable @NonNull Long batchId) {
        // Calls the service method that joins Batch, Farm, and Quality findings
        BatchDetailResponseDto details = batchService.getBatchDetail(batchId);
        return ResponseEntity.ok(details);
    }
}
