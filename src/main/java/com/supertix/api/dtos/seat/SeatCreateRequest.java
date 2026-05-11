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

    @NotNull(message = "Total seats is required")
    private Integer totalSeats;

    /**
     * Optional 0-based row index for this row within the zone grid.
     * If null, rows are auto-indexed by alphabetical order at read time.
     */
    private Integer rowIndex;
}
