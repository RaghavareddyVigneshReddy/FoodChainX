package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "AUDIT") // Keeping it aligned with your SQL schema name
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer auditId;

    @Column(name = "UserID")
    private Integer userId;

    private String action;
    private String resource;

    @CreationTimestamp
    private LocalDateTime timestamp;
}