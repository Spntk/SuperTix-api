package com.supertix.api.dtos.zone;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {

    private Long id;
    private Long eventId;
    private String name;
    private BigDecimal price;
    private String type;
    private String status;
    private Integer capacity;

    // Layout metadata exposed to the frontend (may be null on legacy zones)
    private Integer layoutX;
    private Integer layoutY;
    private Integer layoutWidth;
    private Integer layoutHeight;
    private Integer rowCount;
    private Integer colCount;
    private String stageDirection;
    private Integer displayOrder;

    private Integer rotationDeg;
    private Integer zIndex;
    private String shape;
    private String polygonPoints;
    private String fillColor;
    private String borderColor;
    private Integer labelOffsetX;
    private Integer labelOffsetY;

    private Long parentZoneId;
}
