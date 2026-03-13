package com.cts.foodchainx.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Entity representing a final retail sale transaction.
 * <p>
 * The {@code Sale} entity records the purchase of a product by a consumer.
 * it links the physical inventory and the production batch to a specific user,
 * completing the traceability path from source to end-user.
 * </p>
 */
@Entity
@Table(name = "SALE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    /**
     * Unique transaction identifier for the sale.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SaleID")
    private Long saleId;

    /**
     * Foreign key reference to the specific {@link Inventory} record used for this sale.
     */
    @Column(name = "InventoryID", nullable = false)
    private Long inventoryId;

    /**
     * Foreign key reference to the {@link User} (Consumer) who purchased the product.
     */
    @Column(name = "ConsumerID", nullable = false)
    private Long consumerId;

    /**
     * Foreign key reference to the original {@link ProductionBatch} for tracking purposes.
     */
    @Column(name = "BatchID", nullable = false)
    private Long batchId;

    /**
     * The date the transaction occurred.
     */
    @Column(name = "Date", nullable = false)
    private LocalDate date;

    /**
     * The number of units sold in this transaction.
     */
    @Column(name = "Quantity", nullable = false)
    private Long quantity;

    /**
     * The total price or unit price charged for the sale.
     */
    @Column(name = "Price", nullable = false)
    private Double price;

    /**
     * Associated inventory details.
     * Marked with {@link JsonIgnore} to optimize API responses and avoid circular references.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InventoryID", insertable = false, updatable = false)
    @JsonIgnore
    private Inventory inventory;

    /**
     * Detailed Consumer (User) object.
     * Loaded lazily to improve performance during bulk sale queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ConsumerID", insertable = false, updatable = false)
    @JsonIgnore
    private User consumer;

    /**
     * Detailed Production Batch information for full-chain traceability lookups.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BatchID", insertable = false, updatable = false)
    @JsonIgnore
    private ProductionBatch productionBatch;
}