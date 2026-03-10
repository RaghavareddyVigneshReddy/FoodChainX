package com.cts.FoodChainX.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Integer notificationId;
    private Integer entityId;
    private String message;
    private String category;
    private String status;
    private LocalDateTime createdDate;
}