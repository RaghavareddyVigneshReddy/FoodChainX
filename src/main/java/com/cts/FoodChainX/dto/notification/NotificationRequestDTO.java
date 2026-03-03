package com.cts.FoodChainX.dto.notification; // Ensure this matches the folder exactly

import lombok.Data;

@Data
public class NotificationRequestDTO {
    private Long userId;
    private int entityId;
    private String message;
    private String category;
}