package com.supertix.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.order.AdminOrderResponse;
import com.supertix.api.dtos.order.OrderCreateRequest;
import com.supertix.api.dtos.order.OrderItemResponse;
import com.supertix.api.dtos.order.OrderResponse;
import com.supertix.api.enums.EventStatus;
import com.supertix.api.enums.OrderStatus;
import com.supertix.api.models.EventModel;
import com.supertix.api.models.NotificationModel;
import com.supertix.api.models.OrderItemModel;
import com.supertix.api.models.OrderModel;
import com.supertix.api.models.SeatModel;
import com.supertix.api.models.TicketModel;
import com.supertix.api.models.UserModel;
import com.supertix.api.repositories.EventRepository;
import com.supertix.api.repositories.NotificationRepository;
import com.supertix.api.repositories.OrderItemRepository;
import com.supertix.api.repositories.OrderRepository;
import com.supertix.api.repositories.SeatRepository;
import com.supertix.api.repositories.TicketRepository;
import com.supertix.api.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final OrderItemRepository orderItemRepository;
    private final TicketRepository ticketRepository;
    private final NotificationRepository notificationRepository;
    private final RedisTemplate redisTemplate;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository, EventRepository eventRepository,
            SeatRepository seatRepository, OrderItemRepository orderItemRepository, TicketRepository ticketRepository,
            NotificationRepository notificationRepository, RedisTemplate redisTemplate) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.orderItemRepository = orderItemRepository;
        this.ticketRepository = ticketRepository;
        this.notificationRepository = notificationRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Map<String, Object> createOrder(Long userId, OrderCreateRequest dto) {

        UserModel user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));

        EventModel event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event not found"));

        if (event.getStatus() != EventStatus.ON_SALE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is not on sale");
        }

        List<SeatModel> seats = seatRepository.findAllById(dto.getSeatIds());
        if (seats.size() != dto.getSeatIds().size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Some seats not found");
        }

        for (SeatModel seat : seats) {
            String lockKey = "seat:lock:" + seat.getId();
            String bookedKey = "seat:booked:" + seat.getId();

            if (redisTemplate.opsForValue().get(lockKey) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is already locked");
            }
            if (redisTemplate.opsForValue().get(bookedKey) != null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Seat " + seat.getRowLabel() + seat.getSeatNumber() + " is already booked");
            }
        }

        for (SeatModel seat : seats) {
            redisTemplate.opsForValue().set(
                    "seat:lock:" + seat.getId(),
                    userId.toString(),
                    10, TimeUnit.MINUTES);
        }

        BigDecimal totalAmount = seats.stream().map(seat -> seat.getZone().getPrice()).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        OrderModel order = new OrderModel();
        order.setUser(user);
        order.setEvent(event);
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);

        List<OrderItemModel> items = seats.stream().map(seat -> {
            OrderItemModel item = new OrderItemModel();
            item.setOrder(order);
            item.setSeat(seat);
            item.setPrice(seat.getZone().getPrice());
            return item;
        }).toList();
        orderItemRepository.saveAll(items);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", order.getId());
        response.put("totalAmount", totalAmount);
        response.put("expireAt", order.getExpireAt());
        response.put("message", "Order created successfully, please pay within 10 minutes");
        return response;
    }

    @Transactional
    public Map<String, String> payOrder(Long userId, Long orderId) {

        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to you");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order is not pending");
        }

        if (LocalDateTime.now().isAfter(order.getExpireAt())) {
            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order has expired");
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        List<OrderItemModel> items = orderItemRepository.findByOrderId(orderId);

        List<TicketModel> tickets = items.stream().map(item -> {
            TicketModel ticket = new TicketModel();
            ticket.setOrder(order);
            ticket.setSeat(item.getSeat());

            redisTemplate.delete("seat:lock:" + item.getSeat().getId());

            redisTemplate.opsForValue().set(
                    "seat:booked:" + item.getSeat().getId(),
                    userId.toString());

            return ticket;
        }).toList();

        ticketRepository.saveAll(tickets);

        NotificationModel notification = new NotificationModel();
        notification.setUser(order.getUser());
        notification.setMessage(
                "Payment successful! You have " + items.size() + " ticket(s) for " + order.getEvent().getTitle());
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment successful");
        return response;
    }

    @Transactional
    public Map<String, String> cancelOrder(Long userId, Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to you");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot cancel order with status: " + order.getStatus().name());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<OrderItemModel> items = orderItemRepository.findByOrderId(orderId);
        items.forEach(item -> redisTemplate.delete("seat:lock:" + item.getSeat().getId()));

        NotificationModel notification = new NotificationModel();
        notification.setUser(order.getUser());
        notification.setMessage("Your order for " + order.getEvent().getTitle() + " has been cancelled");
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Order cancelled successfully");
        return response;
    }

    @Transactional
    public OrderResponse getOrderById(Long userId, Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This order does not belong to you");
        }

        List<OrderItemModel> items = orderItemRepository.findByOrderId(order.getId());
        List<OrderItemResponse> itemResponse = items.stream()
                .map(item -> new OrderItemResponse(
                        item.getSeat().getId(),
                        item.getSeat().getRowLabel() + item.getSeat().getSeatNumber(),
                        item.getSeat().getZone().getName(),
                        item.getPrice()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getEvent().getTitle(),
                order.getEvent().getStartDate().toString(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getExpireAt() != null ? order.getExpireAt().toString() : null,
                order.getCreatedAt().toString(),
                itemResponse);
    }

    @Transactional
    public List<OrderResponse> getOrdersById(Long userId) {
        List<OrderModel> orders = orderRepository.findByUserId(userId);

        return orders.stream().map(order -> {
            List<OrderItemModel> items = orderItemRepository.findByOrderId(order.getId());

            List<OrderItemResponse> itemResponse = items.stream()
                    .map(item -> new OrderItemResponse(
                            item.getSeat().getId(),
                            item.getSeat().getRowLabel() + item.getSeat().getSeatNumber(),
                            item.getSeat().getZone().getName(),
                            item.getPrice()))
                    .toList();

            return new OrderResponse(
                    order.getId(),
                    order.getEvent().getTitle(),
                    order.getEvent().getStartDate().toString(),
                    order.getTotalAmount(),
                    order.getStatus().name(),
                    order.getExpireAt() != null ? order.getExpireAt().toString() : null,
                    order.getCreatedAt().toString(), itemResponse);
        }).toList();
    }

    @Transactional
    public List<AdminOrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(order -> {
            List<OrderItemModel> items = orderItemRepository.findByOrderId(order.getId());
            List<OrderItemResponse> itemResponse = items.stream()
                    .map(item -> new OrderItemResponse(
                            item.getSeat().getId(),
                            item.getSeat().getRowLabel() + item.getSeat().getSeatNumber(),
                            item.getSeat().getZone().getName(),
                            item.getPrice()))
                    .toList();
            return new AdminOrderResponse(
                    order.getId(),
                    order.getUser().getId(),
                    order.getUser().getName(),
                    order.getUser().getEmail(),
                    order.getEvent().getId(),
                    order.getEvent().getTitle(),
                    order.getEvent().getStartDate().toString(),
                    order.getTotalAmount(),
                    order.getStatus().name(),
                    order.getExpireAt() != null ? order.getExpireAt().toString() : null,
                    order.getCreatedAt().toString(),
                    itemResponse);
        }).toList();
    }

    @Transactional
    public Map<String, String> adminCancelOrder(Long orderId) {
        OrderModel order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        List<OrderItemModel> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItemModel item : items) {
            try {
                redisTemplate.delete("seat:lock:" + item.getSeat().getId());
                redisTemplate.delete("seat:booked:" + item.getSeat().getId());
            } catch (Exception ignored) {
                // Redis down — DB cancellation already saved
            }
        }

        NotificationModel notification = new NotificationModel();
        notification.setUser(order.getUser());
        notification.setMessage("Your order for " + order.getEvent().getTitle() + " was cancelled by admin");
        notificationRepository.save(notification);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Order cancelled by admin");
        return response;
    }
}
