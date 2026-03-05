package com.cts.FoodChainX.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cts.FoodChainX.dto.batch.BatchDetailResponseDto;
import com.cts.FoodChainX.dto.batch.BatchRequestDto;
import com.cts.FoodChainX.dto.batch.BatchResponseDto;
import com.cts.FoodChainX.service.ProductionBatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/production")
@RequiredArgsConstructor
public class ProductionBatchController {

    private final ProductionBatchService service;
    @Autowired
    private ProductionBatchService batchService;
    // Create a new batch
    @PostMapping("/add")
    public ResponseEntity<BatchResponseDto> createBatch(@RequestBody BatchRequestDto dto) {
        return new ResponseEntity<>(service.createBatch(dto), HttpStatus.CREATED);
    }

    // Get a specific batch by ID
    @GetMapping("/{id}")
    public ResponseEntity<BatchResponseDto> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getBatchById(id));
    }

    // Get all batches for a specific farm
    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<BatchResponseDto>> getBatchesByFarm(@PathVariable Long farmId) {
        return ResponseEntity.ok(service.getBatchesByFarm(farmId));
    }

    // Delete a batch
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBatch(@PathVariable Long id) {
        return ResponseEntity.ok(service.deleteBatch(id));
    }
    @GetMapping("/{batchId}/details")
    public ResponseEntity<BatchDetailResponseDto> getBatchFullDetails(@PathVariable Long batchId) {
        // Calls the service method that joins Batch, Farm, and Quality findings
        BatchDetailResponseDto details = batchService.getBatchDetail(batchId);
        return ResponseEntity.ok(details);
    }
}
