package com.cts.foodchainx.dto.sale;

import lombok.Data;

<<<<<<< HEAD
/**
 * Data Transfer Object (DTO) for capturing sale transaction requests.
 * <p>
 * This class is used to transport sale data from the client to the {@code SaleController}.
 * It focuses strictly on the user-provided inputs required to initiate a purchase.
 * </p>
 */
@Data
public class SaleRequestDTO {

    /**
     * The unique identifier of the inventory item being purchased.
     * This links the sale to a specific retailer's stock.
     */
    private Long inventoryId;

    /**
     * The unique identifier of the consumer making the purchase.
     * Essential for maintaining the "Sold-To" link in traceability records.
     */
    private Long consumerId;

    /**
     * The number of units being purchased in this transaction.
     */
    private Long quantity;

    /**
     * The agreed-upon price for the transaction.
     * Note: In production environments, this is often validated against
     * the current master price list on the server.
     */
    private Double price;

}
=======
@Data
public class SaleRequestDTO {

    private Long inventoryId;

    private Long consumerId;

    private Long quantity;

    private Double price;

}
>>>>>>> d067e7d9657dbb3bba899c6df80c3f723990653e
