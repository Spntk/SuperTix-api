package com.supertix.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supertix.api.dtos.order.OrderCreateRequest;
import com.supertix.api.dtos.order.OrderResponse;
import com.supertix.api.service.OrderService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createOrder(HttpServletRequest request,
            @Valid @RequestBody OrderCreateRequest dto) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.createOrder(userId, dto));
    }

    @PutMapping("/payment/{id}")
    public ResponseEntity<Map<String, String>> payOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.payOrder(userId, id));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.cancelOrder(userId, id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.getOrdersById(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(orderService.getOrderById(userId, id));
    }
}
