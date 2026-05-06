package com.supertix.api.dtos.notification;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
