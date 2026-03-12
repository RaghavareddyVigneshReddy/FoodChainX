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

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
@Slf4j
public class ProductionBatchController {

    private final ProductionBatchService service;
    private final ProductionBatchService batchService;
    // Create a new batch
    @PostMapping("/add")
    public ResponseEntity<BatchResponseDto> createBatch(@Valid @RequestBody BatchRequestDto dto) {
        log.info("REST request to create new Production Batch for Farm ID: {}", dto.getFarmId());
        return new ResponseEntity<>(service.createBatch(dto), HttpStatus.CREATED);
    }

    // Get a specific batch by ID
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponseDto> getBatchById(@PathVariable @NonNull Long id) {
        
        return ResponseEntity.ok(service.getBatchById(id));
    }

    // Get all batches for a specific farm
    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<BatchResponseDto>> getBatchesByFarm(@PathVariable @NonNull Long farmId) {
        return ResponseEntity.ok(service.getBatchesByFarm(farmId));
    }

    // Delete a batch
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBatch(@PathVariable @NonNull Long id) {
        log.warn("REST request to DELETE Production Batch ID: {}", id);
        return ResponseEntity.ok(service.deleteBatch(id));
    }
    @GetMapping("/{batchId}/details")
    public ResponseEntity<BatchDetailResponseDto> getBatchFullDetails(@PathVariable @NonNull Long batchId) {
        // Calls the service method that joins Batch, Farm, and Quality findings
        BatchDetailResponseDto details = batchService.getBatchDetail(batchId);
        return ResponseEntity.ok(details);
    }
}
