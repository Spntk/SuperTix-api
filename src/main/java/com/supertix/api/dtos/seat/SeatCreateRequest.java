package com.supertix.api.dtos.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeatCreateRequest {

    @NotNull(message = "Zone id is required")
    private Long zoneId;

    @NotBlank(message = "Row label is required")
    private String rowLabel;

    @NotNull(message = "Zone id is required")
    private Integer totalSeats;
}
