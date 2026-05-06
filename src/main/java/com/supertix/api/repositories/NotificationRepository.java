package com.supertix.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.models.NotificationModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
    List<NotificationModel> findByUserId(Long userId);
}
