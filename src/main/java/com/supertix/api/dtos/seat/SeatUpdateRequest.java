package com.supertix.api.dtos.seat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SeatUpdateRequest {

    @NotBlank(message = "Row label is required")
    private String rowLabel;

    @NotNull(message = "Seat number is required")
    private Integer seatNumber;

    private Integer rowIndex;
    private Integer colIndex;
    private Integer displayOrder;
}
