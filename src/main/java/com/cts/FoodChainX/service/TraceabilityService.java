package com.cts.FoodChainX.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponse;
import com.cts.FoodChainX.model.TraceRecord;
import com.cts.FoodChainX.repository.TraceRecordRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TraceabilityService {

    private final TraceRecordRepository traceRecordRepository;
@Transactional(readOnly = true)
public TraceRecordResponse getTraceabilityData(Long batchId) {
    // Change _BatchId to _ProductionId to match your Repository
    return traceRecordRepository.findByProductionBatch_ProductionId(batchId)
            .map(this::mapToResponse)
            .orElseThrow(() -> new EntityNotFoundException("Trace history for Batch ID " + batchId + " not found."));
}

private TraceRecordResponse mapToResponse(TraceRecord record) {
    return TraceRecordResponse.builder()
            .traceId(record.getTraceId())
            .batchId(record.getProductionBatch().getProductionId())
            // ✅ Fetch Crop Type from the Production table
            .cropType(record.getProductionBatch().getCropType())
            // ✅ Fetch Farm Name from the Farm table
            .farmName(record.getFarm() != null ? record.getFarm().getName() : "N/A")
            .distributorName(record.getDistributor() != null ? record.getDistributor().getLocation() : "In Transit")
            // ✅ Fetch Consumer Name from the User relationship
            .consumerName(record.getConsumer() != null ? record.getConsumer().getName() : "Not Yet Purchased")
            .status(record.getStatus())
            .date(record.getDate())
            .build();
}

@Transactional(readOnly = true)
public String generateQrPayload(Long batchId) {
    return traceRecordRepository.findByProductionBatch_ProductionId(batchId)
            .map(record -> String.format(
                "FoodChainX-Trace:%d|Batch:%d|Product:%s|Farm:%s|Status:%s",
                record.getTraceId(),
                record.getProductionBatch().getProductionId(),
                record.getProductionBatch().getCropType(),
                record.getFarm() != null ? record.getFarm().getName() : "N/A",
                record.getStatus()
            ))
            .orElseThrow(() -> new EntityNotFoundException("Batch ID " + batchId + " not found."));
}
}