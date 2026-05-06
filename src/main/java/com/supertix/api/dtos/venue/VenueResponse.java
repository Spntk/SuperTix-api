package com.supertix.api.dtos.venue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VenueResponse {

    private Long id;
    private String name;
    private Integer capacity;
    private String imageUrl;
    private String status;
    private String createdAt;
}
