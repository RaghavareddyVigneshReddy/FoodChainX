package com.cts.foodchainx.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.foodchainx.dto.tracerecord.TraceRecordResponseDto;
import com.cts.foodchainx.exception.BatchNotFoundException;
import com.cts.foodchainx.model.TraceRecord;
import com.cts.foodchainx.model.QualityCheck;
import com.cts.foodchainx.repository.QualityLoggingRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for managing product traceability and transparency data.
 * It aggregates information from movement logs and quality inspections to provide 
 * a comprehensive "Farm-to-Table" view for consumers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TraceabilityService {

    private final TraceRecordRepository traceRecordRepository;
    private final QualityLoggingRepository qualityLoggingRepository;

    /**
     * Retrieves the most recent traceability state for a given batch.
     * Uses the latest movement record and attaches current quality certification status.
     *
     * @param batchId the unique ID of the production batch to trace
     * @return the most recent {@link TraceRecordResponseDto}
     * @throws BatchNotFoundException if no trace records exist for the specified batch
     */
    @Transactional(readOnly = true)
    public TraceRecordResponseDto getTraceabilityData(Long batchId) {
        log.debug("Fetching latest traceability record for Batch ID: {}", batchId);
        return traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId)
                .stream()
                .findFirst()
                .map(this::mapToResponse)
                .orElseThrow(() -> new BatchNotFoundException(batchId));
    }

    /**
     * Maps a {@link TraceRecord} entity to a {@link TraceRecordResponseDto}.
     * This method performs data enrichment by querying the latest quality check 
     * results and providing fallbacks for missing supply chain participants.
     *
     * @param record the trace record entity to map
     * @return an enriched response DTO
     */
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
                .isQualityCertified(isCertified)
                .qualityGrade(grade)
                .build();
    }

    /**
     * Generates a rich-text payload for QR code generation.
     * The format uses a piped string (FCX|...) containing critical batch info, 
     * harvest dates, and certification status for quick offline scanning.
     *
     * @param batchId the ID of the batch for which to generate the payload
     * @return a piped string containing batch transparency data
     * @throws EntityNotFoundException if the batch has no history to generate a payload from
     */
    @Transactional(readOnly = true)
    public String generateQrPayload(Long batchId) {
        return traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(batchId)
                .stream()
                .findFirst()
                .map(record -> {
                    boolean certified = qualityLoggingRepository
                        .findFirstByBatch_ProductionIdOrderByDateDesc(batchId)
                        .map(q -> "PASSED".equalsIgnoreCase(q.getStatus()))
                        .orElse(false);

                    String farm = record.getFarm() != null ? record.getFarm().getName() : "N/A";
                    String crop = record.getProductionBatch().getCropType();
                    String harvestDate = record.getProductionBatch().getHarvestDate().toString();
                    String distributor = record.getDistributor() != null ? record.getDistributor().getName() : "In Transit";
                    String retailer = record.getRetailer() != null ? record.getRetailer().getName() : "Local Market";
                    String currentStatus = record.getStatus();

                    return String.format(
                        "FCX|Batch:%d|Prod:%s|Harvest:%s|Farm:%s|Cert:%b|Status:%s|Dist:%s|Ret:%s",
                        batchId, crop, harvestDate, farm, certified, currentStatus, distributor, retailer
                    );
                })
                .orElseThrow(() -> new EntityNotFoundException("Trace history for Batch ID " + batchId + " not found."));
    }

    /**
     * Retrieves the entire journey history of a specific batch.
     * Useful for building a timeline view in the consumer portal.
     *
     * @param batchId the production ID to look up
     * @return a chronological list of all traceability events
     */
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