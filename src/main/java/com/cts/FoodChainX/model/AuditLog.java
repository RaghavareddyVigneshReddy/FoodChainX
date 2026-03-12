package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.*;
//import java.time.LocalDateTime;
//import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "AUDIT_LOGS", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_ts", columnList = "timestamp")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long auditId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // Many logs belong to one user

  @Column(nullable = false, length = 128)
  private String action;

  @Column(nullable = false, length = 256)
  private String resource;

  @Column(nullable = false)
  private Instant timestamp;
}

