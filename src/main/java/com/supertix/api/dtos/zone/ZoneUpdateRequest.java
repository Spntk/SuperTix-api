package com.supertix.api.dtos.zone;

import java.math.BigDecimal;

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
}
