package com.cts.foodchainx.dto.tracerecord;

import lombok.Builder;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a comprehensive traceability record for a product batch.
 * This record is used by the Consumer Transparency Portal to display the journey of food
 * from farm to table, including quality certification details.
 * * @param traceId            The unique identifier for this specific tracking entry.
 * @param batchId            The unique identifier for the production batch.
 * @param cropType           The type of crop (e.g., Tomato, Wheat) associated with the batch.
 * @param farmName           The name of the origin farm; defaults to "N/A" if unknown.
 * @param distributorName    The name of the distributor handling the batch; "In Transit" if currently moving.
 * @param retailerName       The name of the retail outlet; "Local Market" if not yet assigned.
 * @param consumerName       The name of the purchasing consumer; "Available" if still on shelf.
 * @param date               The timestamp of when this specific trace record was created or updated.
 * @param status             The current logistical status (e.g., Harvested, Shipped, Delivered).
 * @param isQualityCertified Indicates if the batch has passed the official quality inspection.
 * @param qualityGrade       The specific findings or grade assigned by the inspector (e.g., Grade A, Organic).
 */
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