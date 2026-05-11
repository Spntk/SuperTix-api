package com.supertix.api.dtos.seat;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {

    private Long id;
    private Long zoneId;
    private String zoneName;
    private String zoneType;
    private BigDecimal price;
    private String rowLabel;
    private Integer seatNumber;
    private String status;

    // Grid coordinates (may be null on legacy seats — frontend infers fallback)
    private Integer rowIndex;
    private Integer colIndex;
    private Integer displayOrder;
}
