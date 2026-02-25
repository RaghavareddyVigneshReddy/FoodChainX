package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String name;
    private String role;

    @Column(unique = true)
    private String email;

    private String phone;
    private String status;
}