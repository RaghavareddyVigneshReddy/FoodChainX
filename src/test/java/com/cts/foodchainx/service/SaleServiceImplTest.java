package com.cts.foodchainx.service;

import com.cts.foodchainx.model.Inventory;
import com.cts.foodchainx.model.Sale;
import com.cts.foodchainx.model.TraceRecord;
import com.cts.foodchainx.model.User;
import com.cts.foodchainx.repository.InventoryRepository;
import com.cts.foodchainx.repository.SaleRepository;
import com.cts.foodchainx.repository.TraceRecordRepository;
import com.cts.foodchainx.repository.UserRepository;

import com.cts.foodchainx.serviceimpl.SaleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraceRecordRepository traceRecordRepository;

    @InjectMocks
    private SaleServiceImpl saleServiceImpl;

    private Sale sale;
    private Inventory inventory;
    private User consumer;
    private TraceRecord traceRecord;

    @BeforeEach
    void setUp() {

        inventory = new Inventory();
        inventory.setInventoryId(1L);
        inventory.setBatchId(1L);
        inventory.setQuantity(100L);

        sale = new Sale();
        sale.setSaleId(1L);
        sale.setInventoryId(1L);
        sale.setBatchId(1L);
        sale.setConsumerId(10L);
        sale.setQuantity(10L);
        sale.setPrice(200.0);

        consumer = new User();
        consumer.setUserId(10L);
        consumer.setName("Consumer");

        traceRecord = new TraceRecord();
    }

    @Test
    void testCreateSale() {

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));
        when(userRepository.findById(10L)).thenReturn(Optional.of(consumer));
        when(traceRecordRepository
                .findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(1L))
                .thenReturn(List.of(traceRecord));

        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale result = saleServiceImpl.createSale(sale);

        assertNotNull(result);
        assertEquals(10L, result.getQuantity());

        verify(saleRepository, times(1)).save(any(Sale.class));
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void testInventoryNotFound() {

        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> saleServiceImpl.createSale(sale));

        assertEquals("Inventory not found with ID: 1", exception.getMessage());
    }

    @Test
    void testInsufficientStock() {

        inventory.setQuantity(5L);

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(inventory));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> saleServiceImpl.createSale(sale));

        assertEquals("Insufficient stock available", exception.getMessage());
    }

}
