package com.supertix.api.dtos.zone;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ZoneResponse {

    private Long id;
    private Long eventId;
    private String name;
    private BigDecimal price;
    private String type;
    private String status;
    private Integer capacity;
}
