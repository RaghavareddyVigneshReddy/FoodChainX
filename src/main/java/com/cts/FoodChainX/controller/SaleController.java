package com.cts.FoodChainX.controller;

import com.cts.FoodChainX.dto.sale.SaleRequestDTO;
import com.cts.FoodChainX.model.Sale;
import com.cts.FoodChainX.service.SaleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/retail/sales")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @PostMapping
    public Sale createSale(@RequestBody SaleRequestDTO dto) {
        Sale sale=new Sale();

        sale.setInventoryId(dto.getInventoryId());
        sale.setConsumerId(dto.getConsumerId());
        sale.setQuantity(dto.getQuantity());
        sale.setPrice(dto.getPrice());
        return saleService.createSale(sale);
    }
}