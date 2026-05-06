package com.supertix.api.dtos.event;

import com.supertix.api.enums.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private Long venueId;
    private String title;
    private String description;
    private String venueName;
    private String startDate;
    private String saleStartDate;
    private String endDate;
    private EventStatus status;
}
