package com.cts.foodchainx.service;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.enums.*;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.*;
import com.cts.foodchainx.exception.WarehouseCapacityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.cts.foodchainx.serviceimpl.LogisticsServiceImpl;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogisticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private TraceRecordRepository traceRecordRepository;

    @InjectMocks
    private LogisticsServiceImpl logisticsService;

    private Shipment shipment;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        User distributor = new User();
        distributor.setUserId(2L);

        ProductionBatch batch = new ProductionBatch();
        batch.setQuantity(100.0);
        batch.setProductionId(50L);

        warehouse = new Warehouse();
        warehouse.setWarehouseId(1L);
        warehouse.setCapacity(200L);
        warehouse.setCurrentStockLevel(50.0);
        warehouse.setDistributor(distributor);

        shipment = new Shipment();
        shipment.setShipmentId(1L);
        shipment.setBatch(batch);
        shipment.setDistributor(distributor);
    }

    @Test
    void testUpdateShipmentStatus_IncreasesWarehouseStock() {
        // Arrange
        ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
        request.setStatus(ShipmentStatus.DELIVERED);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(warehouseRepository.findByDistributor_UserId(2L)).thenReturn(List.of(warehouse));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        // Act
        logisticsService.updateShipmentStatus(1L, request);

        // Assert: 50.0 (initial) + 100.0 (batch) = 150.0
        assertEquals(150.0, warehouse.getCurrentStockLevel());
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void testUpdateShipmentStatus_ThrowsExceptionWhenCapacityExceeded() {
        // Arrange: Initial 150 + Batch 100 = 250 (Exceeds 200 capacity)
        warehouse.setCurrentStockLevel(150.0);
        ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
        request.setStatus(ShipmentStatus.DELIVERED);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));
        when(warehouseRepository.findByDistributor_UserId(2L)).thenReturn(List.of(warehouse));

        // Act & Assert
        assertThrows(WarehouseCapacityException.class, () -> {
            logisticsService.updateShipmentStatus(1L, request);
        });
    }
}