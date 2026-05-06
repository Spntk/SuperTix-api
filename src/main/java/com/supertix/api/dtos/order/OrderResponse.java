package com.supertix.api.dtos.order;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private String eventTitle;
    private String eventStartDate;
    private BigDecimal totalAmount;
    private String status;
    private String expireAt;
    private String createdAt;
    private List<OrderItemResponse> items;
}
