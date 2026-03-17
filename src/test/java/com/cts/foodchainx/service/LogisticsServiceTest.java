package com.cts.foodchainx.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.enums.*;
import com.cts.foodchainx.exception.WarehouseCapacityException;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.*;
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
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for LogisticsService.
 * Fixes: Replaced String statuses with Enum constants to match Service implementation.
 */
@ExtendWith(MockitoExtension.class)
class LogisticsServiceTest {

    @Mock private ShipmentRepository shipmentRepository;
    @Mock private ProductionBatchRepository batchRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private TraceRecordRepository traceRecordRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryRepository inventoryRepository;

    @InjectMocks
    private LogisticsServiceImpl logisticsService;

    private ProductionBatch sampleBatch;
    private User sampleDistributor;
    private ShipmentRequestDTO shipmentRequest;

    @BeforeEach
    void setUp() {
        // Setup a mock Distributor user
        sampleDistributor = new User();
        sampleDistributor.setUserId(201L);
        sampleDistributor.setRole(Role.DISTRIBUTOR); 

        // Setup a mock Production Batch - FIXED: Use QualityStatus Enum
        sampleBatch = new ProductionBatch();
        sampleBatch.setProductionId(501L);
        sampleBatch.setQualityStatus(QualityStatus.PASSED); 
        sampleBatch.setQuantity(500.0);
        sampleBatch.setFarm(new Farm());

        // Create a standard Shipment initiation request
        shipmentRequest = new ShipmentRequestDTO();
        shipmentRequest.setBatchId(501L);
        shipmentRequest.setDistributorId(201L);
        shipmentRequest.setDepartureDate(LocalDate.now());
        shipmentRequest.setArrivalDate(LocalDate.now().plusDays(3));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Initiate Shipment - Success with Compliant Batch")
    void testInitiateShipment_Success() {
        when(batchRepository.findById(501L)).thenReturn(Optional.of(sampleBatch));
        when(userRepository.findById(201L)).thenReturn(Optional.of(sampleDistributor));
        
        Shipment savedShipment = new Shipment();
        savedShipment.setShipmentId(1L);
        savedShipment.setBatch(sampleBatch);
        savedShipment.setDistributor(sampleDistributor);
        savedShipment.setStatus(ShipmentStatus.IN_TRANSIT); // FIXED: Use ShipmentStatus Enum

        when(shipmentRepository.save(any(Shipment.class))).thenReturn(savedShipment);

        ShipmentResponseDTO response = logisticsService.initiateShipment(shipmentRequest);

        assertNotNull(response);
        // FIXED: Assertion uses Enum
        assertEquals(ShipmentStatus.IN_TRANSIT, response.getStatus());
        verify(traceRecordRepository, times(1)).save(any(TraceRecord.class));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Initiate Shipment - Fails on Non-Compliant Quality")
    void testInitiateShipment_NonCompliant_ThrowsException() {
        sampleBatch.setQualityStatus(QualityStatus.REJECTED); // FIXED: Use QualityStatus Enum
        when(batchRepository.findById(501L)).thenReturn(Optional.of(sampleBatch));
        when(userRepository.findById(201L)).thenReturn(Optional.of(sampleDistributor));

        assertThrows(EntityNotFoundException.class, () -> {
            logisticsService.initiateShipment(shipmentRequest);
        });
    }

    @Test
    @DisplayName("Record Delivery - Fails when Warehouse Status is Full")
    void testRecordDelivery_WarehouseFull_ThrowsException() {
        Warehouse fullWarehouse = new Warehouse();
        fullWarehouse.setWarehouseId(10L);
        fullWarehouse.setStatus(WarehouseStatus.FULL); // FIXED: Use WarehouseStatus Enum
        fullWarehouse.setCapacity(1000L);

        DeliveryRequestDTO request = new DeliveryRequestDTO();
        request.setWarehouseId(10L);

        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(fullWarehouse));

        assertThrows(WarehouseCapacityException.class, () -> {
            logisticsService.recordDelivery(request);
        });
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Record Delivery - Success Updates Inventory and Traceability")
    void testRecordDelivery_Success() {
        Warehouse availableWarehouse = new Warehouse();
        availableWarehouse.setStatus(WarehouseStatus.AVAILABLE); // FIXED: Use WarehouseStatus Enum

        Shipment activeShipment = new Shipment();
        activeShipment.setBatch(sampleBatch);
        activeShipment.setDistributor(sampleDistributor);

        User retailer = new User();
        retailer.setUserId(301L);
        retailer.setRole(Role.RETAILER);

        DeliveryRequestDTO request = new DeliveryRequestDTO();
        request.setWarehouseId(10L);
        request.setShipmentId(100L);
        request.setRetailerId(301L);
        request.setDeliveryDate(LocalDate.now());

        when(warehouseRepository.findById(10L)).thenReturn(Optional.of(availableWarehouse));
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(activeShipment));
        when(userRepository.findById(301L)).thenReturn(Optional.of(retailer));

        logisticsService.recordDelivery(request);

        // FIXED: Assertion uses Enum
        assertEquals(ShipmentStatus.DELIVERED, activeShipment.getStatus());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
        verify(traceRecordRepository, times(1)).save(any(TraceRecord.class));
        verify(deliveryRepository, times(1)).save(any(Delivery.class));
    }

    @Test
    @DisplayName("Get All Warehouses - Returns Correct List")
    void testGetAllWarehouses() {
        Warehouse w = new Warehouse();
        w.setWarehouseId(1L);
        w.setCapacity(2500L);
        w.setStatus(WarehouseStatus.AVAILABLE); // FIXED: Use WarehouseStatus Enum

        when(warehouseRepository.findAll()).thenReturn(List.of(w));

        List<WarehouseResponseDTO> warehouses = logisticsService.getAllWarehouses();

        assertFalse(warehouses.isEmpty());
        assertEquals(2500L, warehouses.get(0).getCapacity());
        verify(warehouseRepository, times(1)).findAll();
    }
}