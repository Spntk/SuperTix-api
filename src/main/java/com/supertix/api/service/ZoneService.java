package com.supertix.api.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.zone.ZoneCreateRequest;
import com.supertix.api.dtos.zone.ZoneResponse;
import com.supertix.api.dtos.zone.ZoneUpdateRequest;
import com.supertix.api.enums.EventStatus;
import com.supertix.api.models.EventModel;
import com.supertix.api.models.SeatModel;
import com.supertix.api.models.ZoneModel;
import com.supertix.api.repositories.EventRepository;
import com.supertix.api.repositories.SeatRepository;
import com.supertix.api.repositories.ZoneRepository;

import jakarta.transaction.Transactional;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;

    public ZoneService(ZoneRepository zoneRepository, EventRepository eventRepository, SeatRepository seatRepository) {
        this.zoneRepository = zoneRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
    }

    public Map<String, String> createZone(ZoneCreateRequest dto) {
        EventModel event = eventRepository.findById(dto.getEventId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event id not found"));

        if (event.getStatus() == EventStatus.CANCELLED && event.getStatus() == EventStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This event is cancelled or completed");
        }

        ZoneModel zone = new ZoneModel();
        zone.setEvent(event);
        zone.setName(dto.getName());
        zone.setPrice(dto.getPrice());
        zone.setType(dto.getType());
        zone.setCapacity(dto.getCapacity());

        // Layout — keep nulls where the admin didn't set anything.
        // The frontend renders an explicit "Layout not configured" state in that case.
        zone.setLayoutX(dto.getLayoutX());
        zone.setLayoutY(dto.getLayoutY());
        zone.setLayoutWidth(dto.getLayoutWidth());
        zone.setLayoutHeight(dto.getLayoutHeight());
        zone.setRowCount(dto.getRowCount());
        zone.setColCount(dto.getColCount());
        if (dto.getStageDirection() != null) zone.setStageDirection(dto.getStageDirection());
        if (dto.getDisplayOrder() != null) zone.setDisplayOrder(dto.getDisplayOrder());

        if (dto.getRotationDeg() != null) zone.setRotationDeg(dto.getRotationDeg());
        if (dto.getZIndex() != null) zone.setZIndex(dto.getZIndex());
        if (dto.getShape() != null) zone.setShape(dto.getShape());
        zone.setPolygonPoints(dto.getPolygonPoints());
        zone.setFillColor(dto.getFillColor());
        zone.setBorderColor(dto.getBorderColor());
        if (dto.getLabelOffsetX() != null) zone.setLabelOffsetX(dto.getLabelOffsetX());
        if (dto.getLabelOffsetY() != null) zone.setLabelOffsetY(dto.getLabelOffsetY());

        if (dto.getParentZoneId() != null) {
            ZoneModel parent = zoneRepository.findById(dto.getParentZoneId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent zone not found"));
            if (!parent.getEvent().getId().equals(event.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Parent zone must belong to the same event");
            }
            zone.setParentZone(parent);
        }

        zoneRepository.save(zone);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Create new zone success");
        return response;
    }

    @Transactional
    public List<ZoneResponse> getZonesByEvent(Long eventId) {
        return zoneRepository.findByEventId(eventId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Map<String, String> updateZone(Long zoneId, ZoneUpdateRequest dto) {
        ZoneModel zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zone not found"));

        int totalCapacity = zoneRepository.findByEventId(zone.getEvent().getId())
                .stream()
                .filter(z -> !z.getId().equals(zoneId))
                .mapToInt(ZoneModel::getCapacity)
                .sum();

        int venueCapacity = zone.getEvent().getVenue().getCapacity();

        if (totalCapacity + dto.getCapacity() > venueCapacity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Total zone capacity exceeds venue capacity of " + venueCapacity);
        }

        zone.setName(dto.getName());
        zone.setPrice(dto.getPrice());
        zone.setType(dto.getType());
        zone.setCapacity(dto.getCapacity());
        zone.setStatus(dto.getStatus());

        // Partial layout updates — null leaves the existing value alone.
        if (dto.getLayoutX() != null) zone.setLayoutX(dto.getLayoutX());
        if (dto.getLayoutY() != null) zone.setLayoutY(dto.getLayoutY());
        if (dto.getLayoutWidth() != null) zone.setLayoutWidth(dto.getLayoutWidth());
        if (dto.getLayoutHeight() != null) zone.setLayoutHeight(dto.getLayoutHeight());
        if (dto.getRowCount() != null) zone.setRowCount(dto.getRowCount());
        if (dto.getColCount() != null) zone.setColCount(dto.getColCount());
        if (dto.getStageDirection() != null) zone.setStageDirection(dto.getStageDirection());
        if (dto.getDisplayOrder() != null) zone.setDisplayOrder(dto.getDisplayOrder());

        if (dto.getRotationDeg() != null) zone.setRotationDeg(dto.getRotationDeg());
        if (dto.getZIndex() != null) zone.setZIndex(dto.getZIndex());
        if (dto.getShape() != null) zone.setShape(dto.getShape());
        if (dto.getPolygonPoints() != null) zone.setPolygonPoints(dto.getPolygonPoints());
        if (dto.getFillColor() != null) zone.setFillColor(dto.getFillColor());
        if (dto.getBorderColor() != null) zone.setBorderColor(dto.getBorderColor());
        if (dto.getLabelOffsetX() != null) zone.setLabelOffsetX(dto.getLabelOffsetX());
        if (dto.getLabelOffsetY() != null) zone.setLabelOffsetY(dto.getLabelOffsetY());

        // Parent zone management — supports set, change, and explicit clear.
        if (Boolean.TRUE.equals(dto.getClearParentZone())) {
            zone.setParentZone(null);
        } else if (dto.getParentZoneId() != null) {
            if (dto.getParentZoneId().equals(zone.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Zone cannot be its own parent");
            }
            ZoneModel parent = zoneRepository.findById(dto.getParentZoneId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent zone not found"));
            if (!parent.getEvent().getId().equals(zone.getEvent().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Parent zone must belong to the same event");
            }
            // Cycle detection — walk parent chain, refuse if we'd loop back.
            ZoneModel cursor = parent;
            Set<Long> seen = new HashSet<>();
            seen.add(zone.getId());
            while (cursor != null) {
                if (!seen.add(cursor.getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Setting this parent would create a cycle");
                }
                cursor = cursor.getParentZone();
            }
            zone.setParentZone(parent);
        }

        zoneRepository.save(zone);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update zone successful");
        return response;
    }

    @Transactional
    public Map<String, String> deleteZone(Long zoneId) {
        ZoneModel zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zone not found"));

        // Detach any children pointing to this zone — application-level ON DELETE SET NULL.
        zoneRepository.findByEventId(zone.getEvent().getId()).forEach(z -> {
            if (z.getParentZone() != null && z.getParentZone().getId().equals(zoneId)) {
                z.setParentZone(null);
                zoneRepository.save(z);
            }
        });

        List<SeatModel> seats = seatRepository.findByZoneId(zoneId);
        seatRepository.deleteAll(seats);

        zoneRepository.delete(zone);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete zone successful");
        return response;
    }

    private ZoneResponse toResponse(ZoneModel zone) {
        return new ZoneResponse(
                zone.getId(),
                zone.getEvent().getId(),
                zone.getName(),
                zone.getPrice(),
                zone.getType().name(),
                zone.getStatus() != null ? zone.getStatus().name() : "AVAILABLE",
                zone.getCapacity(),
                zone.getLayoutX(),
                zone.getLayoutY(),
                zone.getLayoutWidth(),
                zone.getLayoutHeight(),
                zone.getRowCount(),
                zone.getColCount(),
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
                zone.getParentZone() != null ? zone.getParentZone().getId() : null);
    }
}
