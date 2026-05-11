package com.supertix.api.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.supertix.api.enums.StageDirection;
import com.supertix.api.enums.ZoneShape;
import com.supertix.api.enums.ZoneStatus;
import com.supertix.api.enums.ZoneType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "zones")
@Getter
@Setter
@NoArgsConstructor
public class ZoneModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private EventModel event;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private ZoneType type;

    @Enumerated(EnumType.STRING)
    private ZoneStatus status = ZoneStatus.AVAILABLE;

    // === Layout metadata for the venue zone map ===
    // Coordinates are in the venue's logical pixel space (see VenueModel.layoutWidth/Height).
    @Column(name = "layout_x")
    private Integer layoutX;

    @Column(name = "layout_y")
    private Integer layoutY;

    @Column(name = "layout_width")
    private Integer layoutWidth;

    @Column(name = "layout_height")
    private Integer layoutHeight;

    // === Seat grid layout inside the zone ===
    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "col_count")
    private Integer colCount;

    /**
     * Where the stage is, *as seen from inside this zone*.
     * Drives how SeatGrid flips/rotates and where it draws the STAGE label.
     * NORTH = stage in front, SOUTH = behind, EAST = right side, WEST = left side.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "stage_direction")
    private StageDirection stageDirection = StageDirection.NORTH;

    /** Lower numbers render first (closer to stage in lists). */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * Rotation in degrees applied to the zone's bounding box on the venue map.
     * 0 = axis-aligned. Useful for diagonal balconies, fan-shaped sections, etc.
     */
    @Column(name = "rotation_deg")
    private Integer rotationDeg = 0;

    /** Z-index for stacking — higher numbers render on top. */
    @Column(name = "z_index")
    private Integer zIndex = 0;

    /** Geometric shape used to render this zone on the venue map. */
    @Enumerated(EnumType.STRING)
    @Column(name = "shape")
    private ZoneShape shape = ZoneShape.RECT;

    /**
     * For shape=POLYGON: JSON array of [x, y] points in venue-canvas coordinates,
     * e.g. "[[100,50],[300,50],[400,200],[80,180]]".
     * Ignored for other shapes.
     */
    @Column(name = "polygon_points", length = 4000)
    private String polygonPoints;

    /** Optional override for the zone fill color (else derived from ZoneType). */
    @Column(name = "fill_color", length = 16)
    private String fillColor;

    /** Optional override for the zone border color (else derived from ZoneType). */
    @Column(name = "border_color", length = 16)
    private String borderColor;

    /** Pixel offset for the zone label relative to the zone center. */
    @Column(name = "label_offset_x")
    private Integer labelOffsetX = 0;

    @Column(name = "label_offset_y")
    private Integer labelOffsetY = 0;

    /**
     * Adjacency / front-back relationship.
     * `parentZone` is the zone DIRECTLY in front of this one (closer to the stage).
     * Used to render "X section" hints between the stage and this zone's seat
     * view, and to model arbitrary chains like Balcony → Mezzanine → Orchestra.
     *
     * Self-referencing FK; ON DELETE SET NULL is enforced at the application
     * layer (see ZoneService.deleteZone).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_zone_id")
    private ZoneModel parentZone;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now().withNano(0);
    }
}
