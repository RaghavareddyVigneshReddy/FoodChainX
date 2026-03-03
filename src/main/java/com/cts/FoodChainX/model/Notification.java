package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private int notificationId;

    @ManyToOne(fetch = FetchType.EAGER) // Change from LAZY to EAGER to ensure user is loaded
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "EntityID")
    private int entityId;

    @Column(name = "Message", nullable = false)
    private String message;

    @Column(name = "Category")
    private String category;

    @Column(name = "Status")
    private String status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = "Unread";
        }
    }
}