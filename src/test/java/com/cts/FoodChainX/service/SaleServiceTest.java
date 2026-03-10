package com.cts.FoodChainX.service;

import com.cts.FoodChainX.model.Inventory;
import com.cts.FoodChainX.model.Sale;
import com.cts.FoodChainX.repository.InventoryRepository;
import com.cts.FoodChainX.repository.SaleRepository;
import com.cts.FoodChainX.repository.TraceRecordRepository;
import com.cts.FoodChainX.repository.UserRepository;
import com.cts.FoodChainX.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TraceRecordRepository traceRecordRepository;

    @InjectMocks
    private SaleService saleService;

    private Inventory inventory;
    private Sale sale;

    @BeforeEach
    void setUp() {

        inventory = new Inventory();
        inventory.setInventoryId(1);
        inventory.setQuantity(100);

        sale = new Sale();
        sale.setSaleId(1);
        sale.setInventoryId(1);
        sale.setConsumerId(10);
        sale.setQuantity(10);
        sale.setPrice(200.0);
    }

    @Test
    void testCreateSale() {

        User user = new User();
        user.setUserId(10L);

        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inventory));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(saleRepository.save(any(Sale.class))).thenReturn(sale);

        Sale result = saleService.createSale(sale);

        assertNotNull(result);
        assertEquals(10, result.getQuantity());

        verify(inventoryRepository).save(any(Inventory.class));
        verify(saleRepository).save(any(Sale.class));
    }


    @Test
    void testInventoryNotFound() {

        when(inventoryRepository.findById(1)).thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> saleService.createSale(sale));

        assertEquals("Inventory not found", exception.getMessage());
    }

    @Test
    void testInsufficientStock() {

        inventory.setQuantity(5);

        when(inventoryRepository.findById(1)).thenReturn(Optional.of(inventory));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> saleService.createSale(sale));

        assertEquals("Insufficient stock available", exception.getMessage());
    }
}
