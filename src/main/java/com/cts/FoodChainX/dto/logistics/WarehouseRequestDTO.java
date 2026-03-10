package com.cts.FoodChainX.dto.logistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequestDTO {

    
    private Long distributorId; 
    
    private String location; 
    
    private Long capacity; 
    private String status;
    
}
