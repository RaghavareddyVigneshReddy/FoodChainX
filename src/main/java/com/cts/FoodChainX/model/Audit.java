package com.cts.FoodChainX.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

<<<<<<< Updated upstream
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

=======
>>>>>>> Stashed changes
@Entity
@Table(name = "AUDIT")
@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< Updated upstream
=======
@Builder
>>>>>>> Stashed changes
public class Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
<<<<<<< Updated upstream
    @Column(name = "AuditID")
    private Integer auditID;

    // Later can be mapped to a Regulator entity
    @Column(name = "RegulatorID", nullable = false)
    private Integer regulatorID;

    @Column(name = "Scope", length = 255, nullable = false)
    private String scope;

    @Column(name = "Findings", columnDefinition = "TEXT")
    private String findings;

    @Column(name = "Date", nullable = false)
    private LocalDate date;

    @Column(name = "Status", length = 50, nullable = false)
    private String status;
}
=======
    private Integer auditId;

    // Mapping as a simple field rather than a Relationship Object
    @Column(name = "UserID")
    private Integer userId;

    private String action;
    private String resource;

    @CreationTimestamp
    private LocalDateTime timestamp;
}

>>>>>>> Stashed changes
