package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponse;
import com.cts.FoodChainX.service.TraceabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consumer")
@RequiredArgsConstructor
public class ConsumerPortalController {

    private final TraceabilityService traceabilityService;

    @GetMapping("/trace/{batchId}")
    public ResponseEntity<TraceRecordResponse> getTraceRecord(@PathVariable Long batchId) {
        return ResponseEntity.ok(traceabilityService.getTraceabilityData(batchId));
    }

    @GetMapping("/qr/{batchId}")
    public ResponseEntity<String> getQrCodePayload(@PathVariable Long batchId) {
        return ResponseEntity.ok(traceabilityService.generateQrPayload(batchId));
    }
}