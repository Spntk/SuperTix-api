package com.supertix.api.dtos.event;

import java.math.BigDecimal;
import java.util.List;

import com.supertix.api.enums.EventStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One-shot payload for the /events/{id}/tickets page.
 *
 * Returns:
 *   - the event header
 *   - the venue layout canvas (size + optional background image)
 *   - every zone with its full layout config (position, size, rotation,
 *     z-index, shape, polygon, fill/border overrides, label offset,
 *     stage direction, parent-zone for adjacency)
 *   - every seat with its grid coordinates + live status
 *   - per-zone, the full chain of "in front of stage" zone ids derived
 *     from parentZone, so the frontend doesn't have to guess from coords.
 *
 * No layout values are synthesized — what the admin configured is what
 * the client sees. Zones lacking layout fields are returned with nulls and
 * the frontend renders a "Layout not configured" state.
 */
public class EventLayoutResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventInfo {
        private Long id;
        private String title;
        private String venueName;
        private String startDate;
        private EventStatus status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VenueLayout {
        private Long id;
        private String name;
        private Integer layoutWidth;
        private Integer layoutHeight;
        private String mapImageUrl;
        // Stage placement (nullable — frontend uses computed defaults if missing)
        private Integer stageX;
        private Integer stageY;
        private Integer stageWidth;
        private Integer stageHeight;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatLayout {
        private Long id;
        private String rowLabel;
        private Integer seatNumber;
        private Integer rowIndex;
        private Integer colIndex;
        private Integer displayOrder;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZoneLayout {
        private Long id;
        private String name;
        private String type;
        private String status;
        private BigDecimal price;
        private Integer capacity;

        // Position / size on the venue map (nullable when not configured)
        private Integer layoutX;
        private Integer layoutY;
        private Integer layoutWidth;
        private Integer layoutHeight;

        // Seat grid + stage placement for the zone-detail view
        private Integer rowCount;
        private Integer colCount;
        private String stageDirection;
        private Integer displayOrder;

        // Visual config (all nullable — frontend falls back to type-based color)
        private Integer rotationDeg;
        private Integer zIndex;
        private String shape;
        private String polygonPoints;
        private String fillColor;
        private String borderColor;
        private Integer labelOffsetX;
        private Integer labelOffsetY;

        // Adjacency
        private Long parentZoneId;
        /**
         * Resolved chain: every zone id from this zone toward the stage.
         * Empty when there's no parent. The frontend uses this list verbatim
         * to render "Section X • Section Y" hints between the stage and the
         * seat grid — no client-side guessing.
         */
        private List<Long> zonesInFrontIds;

        private List<SeatLayout> seats;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Payload {
        private EventInfo event;
        private VenueLayout venue;
        private List<ZoneLayout> zones;
    }
}
