package com.supertix.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.notification.NotificationResponse;
import com.supertix.api.models.NotificationModel;
import com.supertix.api.repositories.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<NotificationResponse> getNotification(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getMessage(),
                        n.getIsRead(),
                        n.getCreatedAt()))
                .toList();
    }

    public Map<String, String> markAsReadById(Long userId, Long notificationId) {
        NotificationModel notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This message does not belong to you");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Read successful");
        return response;
    }

    public Map<String, String> markAsReadAll(Long userId) {
        List<NotificationModel> notification = notificationRepository.findByUserId(userId);

        notification.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notification);

        Map<String, String> response = new HashMap<>();
        response.put("message", "All notification marked as read");
        return response;
    }
}
