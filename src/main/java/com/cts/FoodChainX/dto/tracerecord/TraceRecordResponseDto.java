package com.cts.FoodChainX.dto.tracerecord;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record TraceRecordResponseDto(
    Long traceId,
    Long batchId,
    String cropType,
    String farmName,
    String distributorName,
    String retailerName,
    String consumerName,
    LocalDate date,
    String status,
    boolean isQualityCertified,
    String qualityGrade
) {}