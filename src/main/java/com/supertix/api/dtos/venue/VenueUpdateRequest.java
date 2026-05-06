package com.supertix.api.dtos.venue;

import com.supertix.api.enums.VenueStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VenueUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    private String imageUrl;

    @NotNull(message = "Status is required")
    private VenueStatus status;
}
