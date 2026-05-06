package com.supertix.api.dtos.order;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AdminOrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private Long eventId;
    private String eventTitle;
    private String eventStartDate;
    private BigDecimal totalAmount;
    private String status;
    private String expireAt;
    private String createdAt;
    private List<OrderItemResponse> items;
}
