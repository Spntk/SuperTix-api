package com.supertix.api.dtos.zone;

import java.math.BigDecimal;

import com.supertix.api.enums.StageDirection;
import com.supertix.api.enums.ZoneShape;
import com.supertix.api.enums.ZoneStatus;
import com.supertix.api.enums.ZoneType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ZoneUpdateRequest {

    @NotBlank(message = "Name id is required")
    private String name;

    @NotNull(message = "Price id is required")
    private BigDecimal price;

    @NotNull(message = "Type id is required")
    private ZoneType type;

    @NotNull(message = "Capacity id is required")
    private Integer capacity;

    @NotNull(message = "Status id is required")
    private ZoneStatus status;

    // === Layout (optional — partial updates supported) ===
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

    private Long parentZoneId;

    /**
     * If true and parentZoneId is null, explicitly clear the existing parent.
     * Lets a drag-and-drop editor "detach" a zone from its predecessor.
     */
    private Boolean clearParentZone;
}
