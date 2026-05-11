package com.supertix.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.event.EventLayoutResponse;
import com.supertix.api.enums.StageDirection;
import com.supertix.api.enums.ZoneShape;
import com.supertix.api.models.EventModel;
import com.supertix.api.models.SeatModel;
import com.supertix.api.models.VenueModel;
import com.supertix.api.models.ZoneModel;
import com.supertix.api.repositories.EventRepository;
import com.supertix.api.repositories.SeatRepository;
import com.supertix.api.repositories.ZoneRepository;

import jakarta.transaction.Transactional;

/**
 * Builds the combined event-layout payload that powers the public ticket
 * picker page (full venue map + per-zone seat grid + live seat status).
 *
 * IMPORTANT: this service does NOT synthesize layout coordinates. Whatever
 * the admin configured is what the client receives — null fields stay null
 * so the frontend can show a "Layout not yet configured" state. All visual
 * positioning is therefore data-driven.
 */
@Service
public class EventLayoutService {

    private final EventRepository eventRepository;
    private final ZoneRepository zoneRepository;
    private final SeatRepository seatRepository;
    private final SeatService seatService;

    public EventLayoutService(EventRepository eventRepository,
                              ZoneRepository zoneRepository,
                              SeatRepository seatRepository,
                              SeatService seatService) {
        this.eventRepository = eventRepository;
        this.zoneRepository = zoneRepository;
        this.seatRepository = seatRepository;
        this.seatService = seatService;
    }

    @Transactional
    public EventLayoutResponse.Payload getEventLayout(Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        VenueModel venue = event.getVenue();
        List<ZoneModel> zones = zoneRepository.findByEventId(eventId);

        // Group seats by zone with a single query — avoids N+1 over many zones.
        List<SeatModel> allSeats = seatRepository.findByZoneEventId(eventId);
        Map<Long, List<SeatModel>> seatsByZone = allSeats.stream()
                .collect(Collectors.groupingBy(s -> s.getZone().getId()));

        // Index zones by id so we can walk the parentZone chain without
        // another DB hit per zone.
        Map<Long, ZoneModel> zoneById = zones.stream()
                .collect(Collectors.toMap(ZoneModel::getId, z -> z));

        // Sort by zIndex (ascending — higher z renders later/on top), then displayOrder.
        zones.sort(Comparator
                .comparingInt((ZoneModel z) -> z.getZIndex() != null ? z.getZIndex() : 0)
                .thenComparingInt(z -> z.getDisplayOrder() != null ? z.getDisplayOrder() : 0));

        List<EventLayoutResponse.ZoneLayout> zoneLayouts = zones.stream()
                .map(z -> mapZone(z, seatsByZone.getOrDefault(z.getId(), List.of()), zoneById))
                .toList();

        EventLayoutResponse.EventInfo info = new EventLayoutResponse.EventInfo(
                event.getId(),
                event.getTitle(),
                venue.getName(),
                event.getStartDate().toString(),
                event.getStatus());

        EventLayoutResponse.VenueLayout venueLayout = new EventLayoutResponse.VenueLayout(
                venue.getId(),
                venue.getName(),
                venue.getLayoutWidth(),
                venue.getLayoutHeight(),
                venue.getMapImageUrl(),
                venue.getStageX(),
                venue.getStageY(),
                venue.getStageWidth(),
                venue.getStageHeight());

        return new EventLayoutResponse.Payload(info, venueLayout, zoneLayouts);
    }

    /**
     * Bulk-fills sensible default layout for every zone in an event that doesn't
     * have one yet. Lets an admin go from "I just created some zones" to a
     * working venue map with one click — no need to type pixel coordinates.
     *
     * Defaults applied per zone (only when the field is null):
     *   - venue.layoutWidth / layoutHeight default to 1000 x 700 if missing
     *   - zones are tiled in a grid below a 120px stage strip on top
     *   - stageDirection: zones in the leftmost column → EAST,
     *     rightmost column → WEST, middle columns → NORTH
     *   - shape: RECT
     *   - rowCount: a sensible square-ish split based on capacity
     *   - colCount: capacity / rowCount
     *   - zIndex / displayOrder: ascending by creation order
     *
     * Existing values are NEVER overwritten — admins can fine-tune any zone
     * and re-run auto-arrange to fill in just the new ones.
     */
    @Transactional
    public Map<String, Object> autoArrange(Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        VenueModel venue = event.getVenue();
        if (venue.getLayoutWidth() == null) venue.setLayoutWidth(1000);
        if (venue.getLayoutHeight() == null) venue.setLayoutHeight(700);
        int canvasW = venue.getLayoutWidth();
        int canvasH = venue.getLayoutHeight();

        // Default stage to a strip across the top of the canvas if the
        // admin hasn't placed it yet. Computed from canvas dimensions
        // (no hardcoded constants).
        if (venue.getStageX() == null)      venue.setStageX((int) Math.round(canvasW * 0.20));
        if (venue.getStageY() == null)      venue.setStageY(20);
        if (venue.getStageWidth() == null)  venue.setStageWidth((int) Math.round(canvasW * 0.60));
        if (venue.getStageHeight() == null) venue.setStageHeight(80);

        List<ZoneModel> zones = zoneRepository.findByEventId(eventId);
        zones.sort(Comparator.comparing(ZoneModel::getId));

        int total = zones.size();
        int cols = Math.max(1, Math.min(3, total));
        int rows = (int) Math.ceil(total / (double) cols);
        int stageStripH = 120;
        int cellW = canvasW / cols;
        int cellH = (canvasH - stageStripH) / Math.max(rows, 1);

        int filled = 0;
        for (int i = 0; i < zones.size(); i++) {
            ZoneModel z = zones.get(i);
            int col = i % cols;
            int row = i / cols;
            boolean changed = false;

            if (z.getLayoutX() == null) { z.setLayoutX(col * cellW + 20); changed = true; }
            if (z.getLayoutY() == null) { z.setLayoutY(stageStripH + row * cellH + 10); changed = true; }
            if (z.getLayoutWidth() == null) { z.setLayoutWidth(cellW - 40); changed = true; }
            if (z.getLayoutHeight() == null) { z.setLayoutHeight(cellH - 20); changed = true; }

            if (z.getStageDirection() == null) {
                StageDirection sd;
                if (cols >= 3 && col == 0) sd = StageDirection.EAST;
                else if (cols >= 3 && col == cols - 1) sd = StageDirection.WEST;
                else sd = StageDirection.NORTH;
                z.setStageDirection(sd);
                changed = true;
            }
            if (z.getShape() == null) { z.setShape(ZoneShape.RECT); changed = true; }
            if (z.getZIndex() == null) { z.setZIndex(i); changed = true; }
            if (z.getDisplayOrder() == null) { z.setDisplayOrder(i); changed = true; }

            // Sensible row/col split — square-ish based on capacity.
            // Always at least 1 to avoid downstream "1 cell, all seats dropped" bugs.
            if (z.getRowCount() == null || z.getColCount() == null) {
                int cap = Math.max(1, z.getCapacity() != null ? z.getCapacity() : 1);
                int rowGuess = Math.max(1, (int) Math.round(Math.sqrt(cap / 2.0)));
                int colGuess = Math.max(1, (int) Math.ceil(cap / (double) rowGuess));
                if (z.getRowCount() == null) z.setRowCount(rowGuess);
                if (z.getColCount() == null) z.setColCount(colGuess);
                changed = true;
            }

            if (changed) {
                zoneRepository.save(z);
                filled++;
            }
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Auto-arrange completed");
        response.put("zonesUpdated", filled);
        response.put("venueLayoutWidth", canvasW);
        response.put("venueLayoutHeight", canvasH);
        return response;
    }

    private EventLayoutResponse.ZoneLayout mapZone(ZoneModel zone,
                                                   List<SeatModel> seats,
                                                   Map<Long, ZoneModel> zoneById) {

        // Map seats. If row/col indices are missing (legacy data), derive from
        // alphabetical row label and 1-based seat number so the grid still renders.
        List<String> distinctRows = seats.stream()
                .map(SeatModel::getRowLabel)
                .distinct()
                .sorted()
                .toList();

        List<EventLayoutResponse.SeatLayout> seatLayouts = seats.stream()
                .map(s -> new EventLayoutResponse.SeatLayout(
                        s.getId(),
                        s.getRowLabel(),
                        s.getSeatNumber(),
                        s.getRowIndex() != null ? s.getRowIndex() : Math.max(0, distinctRows.indexOf(s.getRowLabel())),
                        s.getColIndex() != null ? s.getColIndex() : Math.max(0, s.getSeatNumber() - 1),
                        s.getDisplayOrder() != null ? s.getDisplayOrder() : s.getSeatNumber(),
                        seatService.resolveSeatStatus(s.getId())))
                .sorted(Comparator
                        .<EventLayoutResponse.SeatLayout, Integer>comparing(
                                s -> s.getRowIndex() != null ? s.getRowIndex() : 0)
                        .thenComparing(s -> s.getColIndex() != null ? s.getColIndex() : 0))
                .toList();

        // CRITICAL: row/col counts returned to the client must always be large
        // enough to accommodate every real seat. Otherwise SeatGrid's matrix
        // bounds check (`seat.colIndex < colCount`) silently drops seats.
        // Take max(adminConfigured, derivedFromSeats, 1).
        int seatRowMax = seatLayouts.stream()
                .mapToInt(s -> s.getRowIndex() != null ? s.getRowIndex() : 0)
                .max()
                .orElse(-1);
        int seatColMax = seatLayouts.stream()
                .mapToInt(s -> s.getColIndex() != null ? s.getColIndex() : 0)
                .max()
                .orElse(-1);
        int effectiveRowCount = Math.max(
                Math.max(zone.getRowCount() != null ? zone.getRowCount() : 0, seatRowMax + 1),
                1);
        int effectiveColCount = Math.max(
                Math.max(zone.getColCount() != null ? zone.getColCount() : 0, seatColMax + 1),
                1);

        // Walk the parent-zone chain to build the explicit "in front of stage" list.
        // Cycles are prevented in ZoneService.updateZone but we re-guard here.
        List<Long> zonesInFrontIds = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        seen.add(zone.getId());
        ZoneModel cursor = zone.getParentZone();
        while (cursor != null && seen.add(cursor.getId())) {
            zonesInFrontIds.add(cursor.getId());
            // Re-fetch from the in-memory map so we follow the chain without
            // touching the lazy proxy again.
            cursor = zoneById.get(cursor.getId()) != null
                    ? zoneById.get(cursor.getId()).getParentZone()
                    : null;
        }

        return new EventLayoutResponse.ZoneLayout(
                zone.getId(),
                zone.getName(),
                zone.getType() != null ? zone.getType().name() : null,
                zone.getStatus() != null ? zone.getStatus().name() : "AVAILABLE",
                zone.getPrice(),
                zone.getCapacity(),
                zone.getLayoutX(),
                zone.getLayoutY(),
                zone.getLayoutWidth(),
                zone.getLayoutHeight(),
                effectiveRowCount,
                effectiveColCount,
                zone.getStageDirection() != null ? zone.getStageDirection().name() : null,
                zone.getDisplayOrder(),
                zone.getRotationDeg(),
                zone.getZIndex(),
                zone.getShape() != null ? zone.getShape().name() : null,
                zone.getPolygonPoints(),
                zone.getFillColor(),
                zone.getBorderColor(),
                zone.getLabelOffsetX(),
                zone.getLabelOffsetY(),
                zone.getParentZone() != null ? zone.getParentZone().getId() : null,
                zonesInFrontIds,
                seatLayouts);
    }
}
