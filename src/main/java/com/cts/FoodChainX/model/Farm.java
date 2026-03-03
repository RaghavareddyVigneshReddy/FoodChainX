package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "FARM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Farm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FarmID")
    private Long farmId;

    @Column(name = "Name", length = 255)
    private String name;

    @Column(name = "Location", length = 255)
    private String location;

    // Keeping FK as scalar (no cardinality)
   // ✅ ADDED CARDINALITY HERE
    // Many Farms belong to One User (Farmer)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmerid", referencedColumnName = "user_id", nullable = false)
    private User farmer;

    @Column(name = "CertificationStatus", length = 100)
    private String certificationStatus;
}