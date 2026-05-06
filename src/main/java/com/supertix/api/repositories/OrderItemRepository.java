package com.supertix.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.supertix.api.models.OrderItemModel;

public interface OrderItemRepository extends JpaRepository<OrderItemModel, Long> {
    List<OrderItemModel> findByOrderId(Long orderId);
}
