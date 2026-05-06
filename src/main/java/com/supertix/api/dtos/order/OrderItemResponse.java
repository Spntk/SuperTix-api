package com.supertix.api.dtos.order;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OrderItemResponse {

    private Long seatId;
    private String seatLabel;
    private String zoneName;
    private BigDecimal price;
}
