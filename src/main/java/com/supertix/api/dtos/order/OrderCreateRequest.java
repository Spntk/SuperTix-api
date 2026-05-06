package com.supertix.api.dtos.order;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderCreateRequest {

    @NotNull(message = "Event id is required")
    private Long eventId;

    @NotNull(message = "Seats id is required")
    private List<Long> seatIds;
}
