package com.cts.FoodChainX.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.FoodChainX.dto.tracerecord.TraceRecordResponseDto;
import com.cts.FoodChainX.exception.BatchNotFoundException;
import com.cts.FoodChainX.model.TraceRecord;
import com.cts.FoodChainX.model.QualityCheck;
import com.cts.FoodChainX.repository.QualityLoggingRepository;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraceabilityService {

    private final TraceRecordRepository traceRecordRepository;
    private final QualityLoggingRepository qualityLoggingRepository;

    @Transactional(readOnly = true)
    public TraceRecordResponseDto getTraceabilityData(Long batchId) {
        log.debug("Fetching latest traceability record for Batch ID: {}", batchId);
        return traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId)
                .stream()
                .findFirst()
                .map(this::mapToResponse)
                .orElseThrow(() -> new BatchNotFoundException(batchId));
    }

    private TraceRecordResponseDto mapToResponse(TraceRecord record) {
        // Query the latest quality check for this specific batch
        var qualityInfo = qualityLoggingRepository
                .findFirstByBatch_ProductionIdOrderByDateDesc(record.getProductionBatch().getProductionId());

        boolean isCertified = qualityInfo.map(q -> "PASSED".equalsIgnoreCase(q.getStatus())).orElse(false);
        String grade = qualityInfo.map(QualityCheck::getFindings).orElse("Pending Inspection");

        return TraceRecordResponseDto.builder()
                .traceId(record.getTraceId())
                .batchId(record.getProductionBatch().getProductionId())
                .cropType(record.getProductionBatch().getCropType())
                .farmName(record.getFarm() != null ? record.getFarm().getName() : "N/A")
                .distributorName(record.getDistributor() != null ? record.getDistributor().getName() : "In Transit")
                .retailerName(record.getRetailer() != null ? record.getRetailer().getName() : "Local Market")
                .consumerName(record.getConsumer() != null ? record.getConsumer().getName() : "Available")
                .status(record.getStatus())
                .date(record.getDate())
                // Transparency fields mapped here
                .isQualityCertified(isCertified)
                .qualityGrade(grade)
                .build();
    }

    @Transactional(readOnly = true)
    public String generateQrPayload(Long batchId) {
        return traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId)
                .stream()
                .findFirst() // Get the most recent state
                .map(record -> {
                    // 1. Check quality certification
                    boolean certified = qualityLoggingRepository
                        .findFirstByBatch_ProductionIdOrderByDateDesc(batchId)
                        .map(q -> "PASSED".equalsIgnoreCase(q.getStatus()))
                        .orElse(false);

                    // 2. Extract detailed info with fallbacks (consistent with your JSON response)
                    String farm = record.getFarm() != null ? record.getFarm().getName() : "N/A";
                    String crop = record.getProductionBatch().getCropType();
                    String harvestDate = record.getProductionBatch().getHarvestDate().toString();
                    String distributor = record.getDistributor() != null ? record.getDistributor().getName() : "In Transit";
                    String retailer = record.getRetailer() != null ? record.getRetailer().getName() : "Local Market";
                    String currentStatus = record.getStatus();

                    // 3. Generate the rich payload string
                    return String.format(
                        "FCX|Batch:%d|Prod:%s|Harvest:%s|Farm:%s|Cert:%b|Status:%s|Dist:%s|Ret:%s",
                        batchId,
                        crop,
                        harvestDate,
                        farm,
                        certified,
                        currentStatus,
                        distributor,
                        retailer
                    );
                })
                .orElseThrow(() -> new EntityNotFoundException("Trace history for Batch ID " + batchId + " not found."));
    }

    @Transactional(readOnly = true)
    public List<TraceRecordResponseDto> getBatchHistory(Long batchId) {
        log.debug("Fetching full traceability history for Batch ID: {}", batchId);
        
        List<TraceRecord> history = traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId);
        
        if (history.isEmpty()) {
            throw new BatchNotFoundException(batchId);
        }

        return history.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}