package com.cts.FoodChainX.model;


import jakarta.persistence.*;
import lombok.*;

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
    private Integer farmId;

    @Column(name = "Name", length = 255)
    private String name;

    @Column(name = "Location", length = 255)
    private String location;

    // Keeping FK as scalar (no cardinality)
    @Column(name = "FarmerID")
    private Integer farmerId;

    @Column(name = "CertificationStatus", length = 100)
    private String certificationStatus;
}