package com.cts.foodchainx.dto.logistics;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequestDTO {

    
    private Long batchId;  
    private Long distributorId; 
    private LocalDate departureDate;
    private LocalDate arrivalDate;
}