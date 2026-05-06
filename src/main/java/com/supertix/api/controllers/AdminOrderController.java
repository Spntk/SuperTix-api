package com.supertix.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supertix.api.dtos.order.AdminOrderResponse;
import com.supertix.api.service.OrderService;

@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.adminCancelOrder(id));
    }
}
