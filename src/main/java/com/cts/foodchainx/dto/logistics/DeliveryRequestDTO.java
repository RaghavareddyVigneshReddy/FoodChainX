package com.cts.foodchainx.dto.logistics;

import lombok.Data;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a request to record a final delivery to a retailer.
 * This object is used to capture the transaction between the distributor/warehouse and the retail store.
 */
@Data
public class DeliveryRequestDTO {
    /** The unique ID of the shipment being delivered */
    private Long shipmentId;
    
    /** The ID of the warehouse the shipment is departing from */
    private Long warehouseId;
    
    /** The ID of the retailer receiving the goods */
    private Long retailerId;
    
    /** The date the delivery was completed */
    private LocalDate deliveryDate;
}