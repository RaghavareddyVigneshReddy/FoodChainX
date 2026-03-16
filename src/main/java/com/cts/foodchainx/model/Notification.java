package com.cts.foodchainx.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.cts.foodchainx.enums.NotificationStatus;

@Entity
@Table(name = "notification")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Notification_ID")
    private long notificationId;

    @ManyToOne(fetch = FetchType.EAGER) // Change from LAZY to EAGER to ensure user is loaded
    @JoinColumn(name = "User_ID", nullable = false)
    private User user;

    @Column(name = "Entity_ID")
    private long entityId;

    @Column(name = "Message", nullable = false)
    private String message;

    @Column(name = "Category")
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status")
    private NotificationStatus status;

    @Column(name = "CreatedDate")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = NotificationStatus.UNREAD;
        }
    }
}