package com.cts.foodchainx.controller;

import com.cts.foodchainx.dto.sale.SaleRequestDTO;
import com.cts.foodchainx.model.Sale;
import com.cts.foodchainx.serviceimpl.SaleServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for managing retail sales transactions.
 * <p>
 * This controller handles the lifecycle of a sale, connecting consumers
 * to specific inventory items within the food chain ecosystem.
 * </p>
 */
@RestController
@RequestMapping("/api/retail/sales")
@Slf4j
@RequiredArgsConstructor
public class SaleController {

    /**
     * Service responsible for sale-related business logic and persistence.
     */
    private final SaleServiceImpl saleServiceImpl;

    /**
     * Processes and records a new sale transaction.
     * <p>
     * Maps the incoming {@link SaleRequestDTO} to a {@link Sale} entity
     * before passing it to the service layer for processing.
     * </p>
     *
     * @param dto The sale details including inventory ID, consumer ID, quantity, and price.
     * @return The saved {@link Sale} entity containing the generated transaction ID.
     */
    @PostMapping
    public Sale createSale(@RequestBody SaleRequestDTO dto) {
        Sale sale = new Sale();

        sale.setInventoryId(dto.getInventoryId());
        sale.setConsumerId(dto.getConsumerId());
        sale.setQuantity(dto.getQuantity());
        sale.setPrice(dto.getPrice());

        return saleServiceImpl.createSale(sale);
    }
}