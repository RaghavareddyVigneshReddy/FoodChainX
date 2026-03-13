package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity class representing a Farm plot in the food supply chain.
 * <p>This class maps to the "FARM" table in the database and stores 
 * essential data about the farm's location and its certification status.</p>
 */
@Entity
@Table(name = "FARM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    /**
     * Unique identifier for the Farm.
     * Auto-incremented by the database using the Identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FarmID")
    private Long farmId;

    /**
     * The registered name of the farm.
     */
    @Column(name = "Name", length = 255)
    private String name;

    /**
     * The physical address or geographic coordinates of the farm.
     */
    @Column(name = "Location", length = 255)
    private String location;

    /**
     * The User (Farmer) who owns and manages this farm.
     * <p><b>Relationship:</b> Many-to-One. Multiple farms can belong to a single farmer.</p>
     * <p><b>Fetch Type:</b> LAZY - The farmer details are only loaded from the database when specifically accessed.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmerid", referencedColumnName = "user_id", nullable = false)
    private User farmer;

    /**
     * The current regulatory status of the farm (e.g., PENDING, APPROVED, REJECTED).
     */
    @Column(name = "CertificationStatus", length = 100)
    private String certificationStatus;
}