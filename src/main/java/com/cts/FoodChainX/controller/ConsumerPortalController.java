package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponseDto;
import com.cts.FoodChainX.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
@Slf4j
public class ConsumerPortalController {

    private final TraceabilityService traceabilityService;

    @GetMapping("/trace/{batchId}")
    public ResponseEntity<TraceRecordResponseDto> getTraceRecord(@PathVariable Long batchId) {
        log.info("REST request to get traceability data for Batch ID: {}", batchId);
        return ResponseEntity.ok(traceabilityService.getTraceabilityData(batchId));
    }

    @GetMapping("/qr/{batchId}")
    public ResponseEntity<String> getQrCodePayload(@PathVariable Long batchId) {
        log.info("REST request to generate QR payload for Batch ID: {}", batchId);
        return ResponseEntity.ok(traceabilityService.generateQrPayload(batchId));
    }
}