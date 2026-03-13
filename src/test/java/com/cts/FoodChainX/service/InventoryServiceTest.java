package com.cts.foodchainx.service;

import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.repository.InventoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;
    private Inventory inventory;

    @BeforeEach
    void setUp() {

        inventory = new Inventory();
        inventory.setInventoryId(1L);
        inventory.setRetailerId(2L);
        inventory.setBatchId(100L);
        inventory.setQuantity(50L);
        inventory.setStatus("ACTIVE");
    }

    @Test
    void testCreateInventory() {

        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory result = inventoryService.createInventory(inventory);

        assertNotNull(result);
        assertEquals(50, result.getQuantity());

        verify(inventoryRepository).save(inventory);
    }

    @Test
    void testGetInventoryById() {

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        Inventory result = inventoryService.getInventoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getInventoryId());
    }

    @Test
    void testGetAllInventory() {

        List<Inventory> inventoryList = Arrays.asList(inventory);

        when(inventoryRepository.findAll()).thenReturn(inventoryList);

        List<Inventory> result = inventoryService.getAllInventory();

        assertEquals(1, result.size());
    }
}
