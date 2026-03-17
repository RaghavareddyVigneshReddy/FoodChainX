package com.cts.foodchainx.serviceimpl;

import com.cts.foodchainx.aspect.Auditable;
import com.cts.foodchainx.dto.logistics.*;
import com.cts.foodchainx.enums.*;
import com.cts.foodchainx.exception.WarehouseCapacityException;
import com.cts.foodchainx.model.*;
import com.cts.foodchainx.repository.*;
import com.cts.foodchainx.service.LogisticsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Service implementation for managing logistics operations within the FoodChainX ecosystem.
 * This service handles warehouse registration, shipment tracking, and delivery management,
 * ensuring inventory synchronization and traceability across the supply chain.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LogisticsServiceImpl implements LogisticsService {

    private final ShipmentRepository shipmentRepository;
    private final ProductionBatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final DeliveryRepository deliveryRepository;
    private final TraceRecordRepository traceRecordRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * Registers a new warehouse facility associated with a distributor.
     * Initial stock level is set to zero and status is set to AVAILABLE.
     *
     * @param request DTO containing warehouse details such as location, capacity, and distributor ID.
     * @return WarehouseResponseDTO representing the persisted warehouse record.
     * @throws EntityNotFoundException if the distributor ID provided does not exist.
     */
    @Override
    @Transactional
    public WarehouseResponseDTO registerWarehouse(@NonNull WarehouseRequestDTO request) {
        User distributorObj = userRepository.findById(request.getDistributorId())
                .orElseThrow(() -> new EntityNotFoundException("Distributor not found"));

        Warehouse warehouse = new Warehouse();
        warehouse.setDistributor(distributorObj);
        warehouse.setLocation(request.getLocation());
        warehouse.setCapacity(request.getCapacity());
        
        warehouse.setCurrentStockLevel(0.0); 
        warehouse.setStatus(WarehouseStatus.AVAILABLE);

        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("New warehouse registered for distributor {}", distributorObj.getUserId());
        return convertToWarehouseResponseDTO(saved);
    }

    /**
     * Initiates a shipment process for a production batch from a farm to a distributor.
     * Validates that the batch has passed quality checks before allowing shipment.
     *
     * @param request DTO containing shipment dates, batch ID, and distributor ID.
     * @return ShipmentResponseDTO representing the initiated shipment.
     * @throws EntityNotFoundException if the batch or distributor is not found, or if batch quality status is not PASSED.
     */
    @Override
    @Transactional
    @Auditable(action = "INITIATE_SHIPMENT", resource = "LOGISTICS")
    public ShipmentResponseDTO initiateShipment(@NonNull ShipmentRequestDTO request) {
        ProductionBatch batchObj = batchRepository.findById(Objects.requireNonNull(request.getBatchId()))
                .orElseThrow(() -> new EntityNotFoundException("Batch not found"));
        
        User distributorObj = userRepository.findById(Objects.requireNonNull(request.getDistributorId()))
                .orElseThrow(() -> new EntityNotFoundException("Distributor not found"));

        if (batchObj.getQualityStatus() != QualityStatus.PASSED) {
            throw new EntityNotFoundException("Batch is not Compliant. Shipment cannot be initiated.");
        }

        Shipment shipment = new Shipment();
        shipment.setBatch(batchObj);
        shipment.setDistributor(distributorObj);
        shipment.setDepartureDate(request.getDepartureDate());
        shipment.setArrivalDate(request.getArrivalDate());
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        TraceRecord shipmentTrace = new TraceRecord();
        shipmentTrace.setProductionBatch(batchObj);
        shipmentTrace.setFarm(batchObj.getFarm());
        shipmentTrace.setDistributor(distributorObj); 
        shipmentTrace.setStatus(TraceStatus.IN_TRANSIT);
        shipmentTrace.setDate(LocalDate.now());
        traceRecordRepository.save(shipmentTrace);

        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    /**
     * Updates the status of an existing shipment. If status is updated to DELIVERED,
     * the system automatically updates the distributor's warehouse stock levels.
     *
     * @param id The unique identifier of the shipment.
     * @param request DTO containing the new status.
     * @return ShipmentResponseDTO representing the updated shipment.
     * @throws EntityNotFoundException if the shipment or associated warehouse is not found.
     * @throws WarehouseCapacityException if the incoming batch quantity exceeds available warehouse capacity.
     */
    @Override
    @Transactional
    @Auditable(action = "UPDATE_SHIPMENT_STATUS", resource = "LOGISTICS")
    public ShipmentResponseDTO updateShipmentStatus(@NonNull Long id, @NonNull ShipmentStatusUpdateRequest request) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipment record not found"));

        shipment.setStatus(request.getStatus());

        if (request.getStatus() == ShipmentStatus.DELIVERED) {
            Warehouse warehouse = warehouseRepository.findByDistributor_UserId(shipment.getDistributor().getUserId())
                    .stream().findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("No warehouse found for distributor"));

            double incomingQty = shipment.getBatch().getQuantity();
            double updatedStock = warehouse.getCurrentStockLevel() + incomingQty;

            if (updatedStock > (double) warehouse.getCapacity()) {
                throw new WarehouseCapacityException("Cannot receive shipment: Warehouse capacity exceeded");
            }

            warehouse.setCurrentStockLevel(updatedStock);
            if (updatedStock == (double) warehouse.getCapacity()) {
                warehouse.setStatus(WarehouseStatus.FULL);
            }
            warehouseRepository.save(warehouse);

            traceRecordRepository.findByProductionBatch_ProductionIdOrderByDateDescTraceIdDesc(shipment.getBatch().getProductionId())
                .stream().findFirst().ifPresent(trace -> {
                    trace.setStatus(TraceStatus.ARRIVED_AT_WAREHOUSE);
                    traceRecordRepository.save(trace);
                });
        }
        
        return convertToShipmentResponseDTO(shipmentRepository.save(shipment));
    }

    /**
     * Records the final delivery of a production batch to a retailer.
     * This operation reduces warehouse stock, updates shipment status, 
     * and initializes the retailer's inventory record.
     *
     * @param request DTO containing IDs for the warehouse, shipment, and retailer, along with the delivery date.
     * @throws EntityNotFoundException if any of the requested IDs do not match existing records.
     */
    @Override
    @Transactional
    @Auditable(action = "RECORD_DELIVERY", resource = "LOGISTICS")
    public void recordDelivery(@NonNull DeliveryRequestDTO request) {
        Warehouse warehouse = warehouseRepository.findById(Objects.requireNonNull(request.getWarehouseId()))
                .orElseThrow(() -> new EntityNotFoundException("Warehouse not found"));

        Shipment shipment = shipmentRepository.findById(Objects.requireNonNull(request.getShipmentId()))
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found"));

        User retailerObj = userRepository.findById(Objects.requireNonNull(request.getRetailerId()))
                .orElseThrow(() -> new EntityNotFoundException("Retailer not found"));

        ProductionBatch batch = shipment.getBatch();

        double departingQty = batch.getQuantity();
        double updatedStock = Math.max(0.0, warehouse.getCurrentStockLevel() - departingQty);
        warehouse.setCurrentStockLevel(updatedStock);

        if (updatedStock < (double) warehouse.getCapacity()) {
            warehouse.setStatus(WarehouseStatus.AVAILABLE);
        }
        warehouseRepository.save(warehouse);

        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipmentRepository.save(shipment);

        Delivery delivery = new Delivery();
        delivery.setShipment(shipment);
        delivery.setRetailer(retailerObj);
        delivery.setDate(request.getDeliveryDate());
        delivery.setStatus(ShipmentStatus.DELIVERED);
        deliveryRepository.save(delivery);

        Inventory newInventory = new Inventory();
        newInventory.setBatchId(batch.getProductionId());
        newInventory.setRetailerId(retailerObj.getUserId()); 
        long quantity = batch.getQuantity().longValue(); 
        newInventory.setQuantity(quantity); 
        newInventory.setDateAdded(LocalDate.now());
        newInventory.setStatus(quantity == 0 ? InventoryStatus.OUT_OF_STOCK : 
                               quantity <= 10 ? InventoryStatus.LOW_STOCK : InventoryStatus.AVAILABLE);
        
        inventoryRepository.save(newInventory);

        TraceRecord deliveryTrace = new TraceRecord();
        deliveryTrace.setProductionBatch(batch);
        deliveryTrace.setFarm(batch.getFarm());
        deliveryTrace.setDistributor(shipment.getDistributor());
        deliveryTrace.setRetailer(retailerObj);
        deliveryTrace.setStatus(TraceStatus.ON_SHELF_AT_STORE);
        deliveryTrace.setDate(LocalDate.now());
        traceRecordRepository.save(deliveryTrace);
    }

    /**
     * Retrieves a list of all registered warehouses.
     *
     * @return List of WarehouseResponseDTOs.
     */
    @Override
    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::convertToWarehouseResponseDTO)
                .toList();
    }

    /**
     * Helper method to convert a Warehouse entity to a WarehouseResponseDTO.
     *
     * @param w The warehouse entity.
     * @return The populated DTO.
     */
    private WarehouseResponseDTO convertToWarehouseResponseDTO(Warehouse w) {
        return WarehouseResponseDTO.builder()
                .warehouseId(w.getWarehouseId())
                .location(w.getLocation())
                .capacity(w.getCapacity())
                .status(w.getStatus())
                .build();
    }

    /**
     * Helper method to convert a Shipment entity to a ShipmentResponseDTO.
     *
     * @param s The shipment entity.
     * @return The populated DTO.
     */
    private ShipmentResponseDTO convertToShipmentResponseDTO(Shipment s) {
        return ShipmentResponseDTO.builder()
                .shipmentId(s.getShipmentId())
                .batchId(s.getBatch().getProductionId())
                .distributorId(s.getDistributor().getUserId())
                .status(s.getStatus())
                .departureDate(s.getDepartureDate())
                .arrivalDate(s.getArrivalDate())
                .build();
    }
}