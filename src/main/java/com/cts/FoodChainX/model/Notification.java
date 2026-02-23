package com.cts.FoodChainX.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data               
@NoArgsConstructor  
@AllArgsConstructor 
@Builder            
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NotificationID")
    private int notificationId;

    @Column(name = "UserID", nullable = false)
    private int userId;

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