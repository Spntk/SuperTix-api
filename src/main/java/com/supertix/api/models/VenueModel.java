package com.supertix.api.models;

import java.time.LocalDateTime;

import com.supertix.api.enums.VenueStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
public class VenueModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(length = 1000)
    private String imageUrl;

    @Column
    private String imageFileId;

    // === Venue layout canvas (used by the zone map) ===
    // Logical pixel size used to position zones in the venue diagram.
    // Frontend scales this to fit available width while keeping aspect ratio.
    @Column(name = "layout_width")
    private Integer layoutWidth = 1000;

    @Column(name = "layout_height")
    private Integer layoutHeight = 700;

    /**
     * Optional background image for the venue map (floor plan, photo, etc.).
     * Drawn under the zones in the SVG canvas.
     */
    @Column(name = "map_image_url", length = 1000)
    private String mapImageUrl;

    // === Stage placement on the venue map ===
    // The stage is a feature of the venue, not a zone. If null, the frontend
    // computes a sensible default strip across the top of the canvas.
    @Column(name = "stage_x")
    private Integer stageX;

    @Column(name = "stage_y")
    private Integer stageY;

    @Column(name = "stage_width")
    private Integer stageWidth;

    @Column(name = "stage_height")
    private Integer stageHeight;

    @Enumerated(EnumType.STRING)
    private VenueStatus status = VenueStatus.ACTIVE;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now().withNano(0);
    }
}
