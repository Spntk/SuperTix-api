package com.supertix.api.dtos.venue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VenueCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private String imageUrl;
}
