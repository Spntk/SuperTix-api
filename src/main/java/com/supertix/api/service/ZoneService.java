package com.supertix.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        zoneRepository.save(zone);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Create new zone success");
        return response;
    }

    @Transactional
    public List<ZoneResponse> getZonesByEvent(Long eventId) {
        return zoneRepository.findByEventId(eventId)
                .stream()
                .map(zone -> new ZoneResponse(
                        zone.getId(),
                        zone.getEvent().getId(),
                        zone.getName(),
                        zone.getPrice(),
                        zone.getType().name(),
                        zone.getStatus() != null ? zone.getStatus().name() : "AVAILABLE",
                        zone.getCapacity()))
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
        zoneRepository.save(zone);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update zone successful");
        return response;
    }

    @Transactional
    public Map<String, String> deleteZone(Long zoneId) {
        ZoneModel zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zone not found"));

        List<SeatModel> seats = seatRepository.findByZoneId(zoneId);
        seatRepository.deleteAll(seats);

        zoneRepository.delete(zone);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete zone successful");
        return response;
    }
}
