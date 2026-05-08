package com.supertix.api.dtos.event;

import java.time.LocalDateTime;

import com.supertix.api.enums.EventStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EventUpdateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String imageUrl;

    private String imageFileId;

    @NotNull(message = "Venue is required")
    private Long venueId;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "Sale start date is required")
    private LocalDateTime saleStartDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @NotNull(message = "Status is required")
    private EventStatus status;
}
