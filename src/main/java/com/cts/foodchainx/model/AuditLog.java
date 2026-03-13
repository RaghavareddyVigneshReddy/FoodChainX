package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Entity representing a system-wide audit event.
 * <p>
 * This class captures who performed an action, what the action was, 
 * and which resource was affected. Indexed on user_id and timestamp 
 * to optimize administrative reporting.
 * </p>
 */
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

  /**
   * The user responsible for the event. 
   * Uses {@link FetchType#LAZY} to prevent loading user details unless explicitly accessed.
   */
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  /**
   * Descriptive name of the operation (e.g., "UPDATE_FARM_STATUS").
   */
  @Column(nullable = false, length = 128)
  private String action;

  /**
   * The identifier or path of the resource modified (e.g., "farm/500").
   */
  @Column(nullable = false, length = 256)
  private String resource;

  /**
   * The precise UTC moment the event was recorded.
   */
  @Column(nullable = false)
  private Instant timestamp;
}