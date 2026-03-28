package com.cts.foodchainx.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.enums.*;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.*;
import com.cts.foodchainx.exception.WarehouseCapacityException;
import com.cts.foodchainx.serviceimpl.LogisticsServiceImpl;
import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LogisticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private TraceRecordRepository traceRecordRepository;
    @Mock private ProductionBatchRepository batchRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private DeliveryRepository deliveryRepository;

    @InjectMocks
    private LogisticsServiceImpl logisticsService;

    private User distributor;
    private ProductionBatch sampleBatch;
    private Shipment sampleShipment;
    private Warehouse sampleWarehouse;

    @BeforeEach
    void setUp() {
        // Setup Distributor
        distributor = new User();
        distributor.setUserId(2L);
        distributor.setName("Joshna");

        // Setup Batch
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(50L);
        sampleBatch.setQuantity(100.0);
        sampleBatch.setQualityStatus(QualityStatus.PASSED);

        // Setup Warehouse
        sampleWarehouse = new Warehouse();
        sampleWarehouse.setWarehouseId(10L);
        sampleWarehouse.setCapacity(200L);
        sampleWarehouse.setCurrentStockLevel(50.0);
        sampleWarehouse.setDistributor(distributor);
        sampleWarehouse.setStatus(WarehouseStatus.AVAILABLE);

        // Setup Shipment
        sampleShipment = new Shipment();
        sampleShipment.setShipmentId(1L);
        sampleShipment.setBatch(sampleBatch);
        sampleShipment.setDistributor(distributor);
        sampleShipment.setStatus(ShipmentStatus.IN_TRANSIT);
    }

    @Test
    @DisplayName("Initiate Shipment - Fails on Non-Compliant Quality")
    void testInitiateShipment_NonCompliant_ThrowsException() {
        sampleBatch.setQualityStatus(QualityStatus.REJECTED);
        ShipmentRequestDTO request = new ShipmentRequestDTO();
        request.setBatchId(50L);
        request.setDistributorId(2L);

        when(batchRepository.findById(50L)).thenReturn(Optional.of(sampleBatch));
        when(userRepository.findById(2L)).thenReturn(Optional.of(distributor));

        assertThrows(EntityNotFoundException.class, () -> {
            logisticsService.initiateShipment(request);
        });
    }

    @Test
    @DisplayName("Update Shipment Status - Throws Exception When Capacity Exceeded")
    void testUpdateShipmentStatus_CapacityExceeded() {
        // Arrange: 150 (current) + 100 (batch) = 250 (Exceeds 200 capacity)
        sampleWarehouse.setCurrentStockLevel(150.0);
        
        ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
        request.setStatus(ShipmentStatus.DELIVERED);
        request.setWarehouseId(10L); // Set the ID as required by new logic

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(sampleShipment));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(sampleWarehouse));

        // Act & Assert
        assertThrows(WarehouseCapacityException.class, () -> {
            logisticsService.updateShipmentStatus(1L, request);
        });
    }

    @Test
    @DisplayName("Update Shipment Status - Throws IllegalStateException on Unauthorized Warehouse")
    void testUpdateShipmentStatus_UnauthorizedWarehouse() {
        User rogueDistributor = new User();
        rogueDistributor.setUserId(99L); // Different ID
        sampleWarehouse.setDistributor(rogueDistributor);

        ShipmentStatusUpdateRequest request = new ShipmentStatusUpdateRequest();
        request.setStatus(ShipmentStatus.DELIVERED);
        request.setWarehouseId(10L);

        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(sampleShipment));
        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(sampleWarehouse));

        assertThrows(IllegalStateException.class, () -> {
            logisticsService.updateShipmentStatus(1L, request);
        });
    }

    @Test
    @DisplayName("Record Delivery - Success Updates Inventory and Traceability")
    void testRecordDelivery_Success() {
        // Setup Retailer
        User retailer = new User();
        retailer.setUserId(301L);

        DeliveryRequestDTO request = new DeliveryRequestDTO();
        request.setWarehouseId(10L);
        request.setShipmentId(1L);
        request.setRetailerId(301L);
        request.setDeliveryDate(LocalDate.now());

        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(sampleWarehouse));
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(sampleShipment));
        when(userRepository.findById(301L)).thenReturn(Optional.of(retailer));

        logisticsService.recordDelivery(request);

        assertEquals(ShipmentStatus.DELIVERED, sampleShipment.getStatus());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(traceRecordRepository, times(1)).save(any(TraceRecord.class));
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }
}