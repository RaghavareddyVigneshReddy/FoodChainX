package com.cts.FoodChainX.dto.tracerecord;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record TraceRecordResponse(
    Integer traceId,
    Integer batchId,
    String cropType,
    String farmName,
    String distributorName,
    Integer retailerId,
    String consumerName,
    LocalDate date,
    String status
) {}