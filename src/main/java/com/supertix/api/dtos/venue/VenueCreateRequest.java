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
public class VenueCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Capacity is required")
    private Integer capacity;

    @NotNull(message = "Address is required")
    private String address;

    private String imageUrl;

    private String imageFileId;

    @NotNull(message = "Status is required")
    private VenueStatus status;

    // === Layout config ===
    private Integer layoutWidth;
    private Integer layoutHeight;
    /** Optional venue-map background image (floor plan / photo). */
    private String mapImageUrl;

    // === Stage placement on the venue map (all optional) ===
    private Integer stageX;
    private Integer stageY;
    private Integer stageWidth;
    private Integer stageHeight;
}
