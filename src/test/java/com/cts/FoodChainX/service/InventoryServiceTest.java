package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.repository.InventoryRepository;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    public InventoryServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateInventory() {

        Inventory inventory = new Inventory();
        inventory.setRetailerId(1);
        inventory.setBatchId(100);
        inventory.setQuantity(50);

        when(inventoryRepository.save(inventory)).thenReturn(inventory);

        Inventory savedInventory = inventoryService.createInventory(inventory);

        assertNotNull(savedInventory);
        assertEquals(50, savedInventory.getQuantity());
    }

    @Test
    void testGetInventoryById() {

        Inventory inventory = new Inventory();
        inventory.setInventoryId(1);
        inventory.setQuantity(100);

        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryById(1);

        assertEquals(100, result.getQuantity());
    }
}
