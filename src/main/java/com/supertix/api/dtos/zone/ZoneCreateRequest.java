package com.supertix.api.dtos.zone;

import java.math.BigDecimal;

import com.supertix.api.enums.ZoneType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
}
