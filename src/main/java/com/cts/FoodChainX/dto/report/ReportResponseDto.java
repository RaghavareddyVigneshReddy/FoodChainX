package com.cts.foodchainx.dto.report;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Long reportId;
    private String scope; // e.g., "FARM", "DISTRIBUTOR", "RETAILER"
    private Map<String, Object> metrics; // e.g., {"totalBatches": 50, "complianceRate": "95%"}
    private LocalDateTime generatedDate;
}