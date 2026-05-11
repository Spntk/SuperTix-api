package com.supertix.api.dtos.venue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VenueResponse {

    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private String imageUrl;
    private String status;
    private String createdAt;

    // Layout canvas size used for rendering the venue zone map
    private Integer layoutWidth;
    private Integer layoutHeight;
    private String mapImageUrl;

    private Integer stageX;
    private Integer stageY;
    private Integer stageWidth;
    private Integer stageHeight;
}
