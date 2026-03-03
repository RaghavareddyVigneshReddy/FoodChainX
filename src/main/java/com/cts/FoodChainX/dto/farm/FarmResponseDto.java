package com.cts.FoodChainX.dto.farm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FarmResponseDto{
    private Long farmId;
    private String name;
    private String location;
    private String certificationStatus;
}
