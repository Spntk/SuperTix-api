package com.supertix.api.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.enums.OrderStatus;
import com.supertix.api.models.OrderModel;

public interface OrderRepository extends JpaRepository<OrderModel, Long> {
    List<OrderModel> findByUserId(Long userId);

    List<OrderModel> findByStatusAndExpireAtBefore(OrderStatus status, LocalDateTime time);
}
