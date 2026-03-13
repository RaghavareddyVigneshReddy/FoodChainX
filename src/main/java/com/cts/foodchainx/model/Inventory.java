package com.cts.foodchainx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entity representing a retail inventory record.
 * <p>
 * This class maps to the {@code INVENTORY} table and tracks the stock levels
 * of specific production batches assigned to a retailer.
 * </p>
 */
@Entity
@Table(name = "INVENTORY")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    /**
     * Unique identifier for the inventory record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "InventoryID")
    private Long inventoryId;

    /**
     * Foreign key reference to the retailer (User).
     */
    @Column(name = "RetailerID", nullable = false)
    private Long retailerId;

    /**
     * Foreign key reference to the production batch.
     */
    @Column(name = "BatchID", nullable = false)
    private Long batchId;

    /**
     * Current stock count available at the retail location.
     */
    @Column(name = "Quantity", nullable = false)
    private Long quantity;

    /**
     * The date this inventory record was first created/received.
     */
    @Column(name = "DateAdded", nullable = false)
    private LocalDate dateAdded;

    /**
     * Current status of the stock (e.g., AVAILABLE, LOW_STOCK, OUT_OF_STOCK).
     */
    @Column(name = "Status", nullable = false)
    private String status;

    /**
     * The User entity acting as the retailer.
     * <p>
     * Managed via Lazy Loading. Ignored in JSON serialization to prevent
     * recursive loops and unnecessary data transfer.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RetailerID", insertable = false, updatable = false)
    @JsonIgnore
    private User retailer;

    /**
     * The production batch associated with this inventory.
     * <p>
     * Managed via Lazy Loading. Ignored in JSON serialization.
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BatchID", insertable = false, updatable = false)
    @JsonIgnore
    private ProductionBatch productionBatch;
}