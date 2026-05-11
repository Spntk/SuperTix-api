package com.supertix.api.dtos.zone;

import java.math.BigDecimal;

import com.supertix.api.enums.StageDirection;
import com.supertix.api.enums.ZoneShape;
import com.supertix.api.enums.ZoneType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Create-zone payload. All layout fields are optional so the basic admin form
 * still works; a future drag-and-drop layout editor will supply the full set.
 */
@Getter
@Setter
@NoArgsConstructor
public class ZoneCreateRequest {

    @NotNull(message = "Event id is required")
    private Long eventId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Price id is required")
    private BigDecimal price;

    @NotNull(message = "Type id is required")
    private ZoneType type;

    @NotNull(message = "capacity id is required")
    private Integer capacity;

    // === Layout (all optional) ===
    private Integer layoutX;
    private Integer layoutY;
    private Integer layoutWidth;
    private Integer layoutHeight;
    private Integer rowCount;
    private Integer colCount;
    private StageDirection stageDirection;
    private Integer displayOrder;

    private Integer rotationDeg;
    private Integer zIndex;
    private ZoneShape shape;
    private String polygonPoints;
    private String fillColor;
    private String borderColor;
    private Integer labelOffsetX;
    private Integer labelOffsetY;

    /** Front/back adjacency — id of the zone directly in front of this one (closer to stage). */
    private Long parentZoneId;
}
