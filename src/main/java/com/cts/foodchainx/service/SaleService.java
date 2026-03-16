package com.cts.foodchainx.service;

import com.cts.foodchainx.model.Sale;

/**
 * Service interface defining the business logic for {@link Sale} transactions.
 * This service handles the processing and recording of sales within the FoodChainX ecosystem.
 */
public interface SaleService {

    /**
     * Processes and records a new sale transaction.
     * <p>
     * Implementation should typically handle:
     * <ul>
     * <li>Validating the sale details.</li>
     * <li>Persisting the sale record to the database.</li>
     * <li>Updating relevant inventory levels (if applicable).</li>
     * </ul>
     * </p>
     *
     * @param sale The {@link Sale} object containing transaction details such as items,
     * quantities, and retailer information.
     * @return The persisted {@link Sale} entity, including its assigned transaction ID
     * and timestamp.
     */
    Sale createSale(Sale sale);

}