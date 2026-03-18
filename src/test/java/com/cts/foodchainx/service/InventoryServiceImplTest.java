package com.cts.foodchainx.service;

import com.cts.foodchainx.enums.InventoryStatus; // Import the new Enum
import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.repository.InventoryRepository;
import com.cts.foodchainx.exception.InventoryNotFoundException;

import com.cts.foodchainx.serviceimpl.InventoryServiceImpl;
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
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryServiceImpl;

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setInventoryId(1L);
        inventory.setRetailerId(2L);
        inventory.setBatchId(100L);
        inventory.setQuantity(50L);
        // Updated to use Enum
        inventory.setStatus(InventoryStatus.AVAILABLE); 
    }

    @SuppressWarnings("null")
    @Test
    void testCreateInventory_ShouldSetStatusToAvailable() {
        // We expect the service to calculate status based on quantity (50 > 10)
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        Inventory result = inventoryServiceImpl.createInventory(inventory);

        assertNotNull(result);
        assertEquals(InventoryStatus.AVAILABLE, result.getStatus());
        verify(inventoryRepository).save(inventory);
    }

    @SuppressWarnings("null")
    @Test
    void testCreateInventory_ShouldSetStatusToLowStock() {
        // Setup inventory with low quantity
        inventory.setQuantity(5L);
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArguments()[0]);

        Inventory result = inventoryServiceImpl.createInventory(inventory);

        assertEquals(InventoryStatus.LOW_STOCK, result.getStatus());
    }

    @Test
    void testGetInventoryById_Success() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        Inventory result = inventoryServiceImpl.getInventoryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getInventoryId());
    }

    @Test
    void testGetInventoryById_NotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(InventoryNotFoundException.class, () -> {
            inventoryServiceImpl.getInventoryById(99L);
        });
    }

    @Test
    void testGetAllInventory() {
        List<Inventory> inventoryList = Arrays.asList(inventory);
        when(inventoryRepository.findAll()).thenReturn(inventoryList);

        List<Inventory> result = inventoryServiceImpl.getAllInventory();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(InventoryStatus.AVAILABLE, result.get(0).getStatus());
    }
}