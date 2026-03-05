package com.cts.FoodChainX.dto.quality;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QualityResponseDto {
 private Long qualityId;
 private LocalDate date;
 private String status;   
 private String findings;
}
