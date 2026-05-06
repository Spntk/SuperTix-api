package com.supertix.api.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.seat.SeatCreateRequest;
import com.supertix.api.dtos.seat.SeatResponse;
import com.supertix.api.dtos.seat.SeatUpdateRequest;
import com.supertix.api.models.SeatModel;
import com.supertix.api.models.ZoneModel;
import com.supertix.api.repositories.SeatRepository;
import com.supertix.api.repositories.ZoneRepository;

import jakarta.transaction.Transactional;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final ZoneRepository zoneRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public SeatService(SeatRepository seatRepository, ZoneRepository zoneRepository,
            RedisTemplate<String, Object> redisTemplate) {
        this.seatRepository = seatRepository;
        this.zoneRepository = zoneRepository;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public Map<String, String> createSeats(SeatCreateRequest dto) {
        ZoneModel zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Zone not found"));

        if (dto.getTotalSeats() == null || dto.getTotalSeats() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total seats must be greater than 0");
        }

        long existingCount = seatRepository.countByZoneId(zone.getId());
        if (existingCount + dto.getTotalSeats() > zone.getCapacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Total seats (" + (existingCount + dto.getTotalSeats())
                            + ") would exceed zone capacity of " + zone.getCapacity());
        }

        List<SeatModel> seats = new ArrayList<>();
        for (int i = 1; i <= dto.getTotalSeats(); i++) {
            if (seatRepository.existsByZoneIdAndRowLabelAndSeatNumber(zone.getId(), dto.getRowLabel(), i)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Seat " + dto.getRowLabel() + i + " already exists in this zone");
            }
            SeatModel seat = new SeatModel();
            seat.setZone(zone);
            seat.setRowLabel(dto.getRowLabel());
            seat.setSeatNumber(i);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Create " + seats.size() + " seat(s) successful");
        return response;
    }

    @Transactional
    public List<SeatResponse> getSeatsByZone(Long zoneId) {
        ZoneModel zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zone not found"));
        return mapSeats(seatRepository.findByZoneId(zone.getId()));
    }

    @Transactional
    public List<SeatResponse> getSeatsByEvent(Long eventId) {
        return mapSeats(seatRepository.findByZoneEventId(eventId));
    }

    @Transactional
    public Map<String, String> updateSeat(Long seatId, SeatUpdateRequest dto) {
        SeatModel seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        boolean changed = !seat.getRowLabel().equals(dto.getRowLabel())
                || !seat.getSeatNumber().equals(dto.getSeatNumber());

        if (changed && seatRepository.existsByZoneIdAndRowLabelAndSeatNumber(
                seat.getZone().getId(), dto.getRowLabel(), dto.getSeatNumber())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Seat " + dto.getRowLabel() + dto.getSeatNumber() + " already exists in this zone");
        }

        seat.setRowLabel(dto.getRowLabel());
        seat.setSeatNumber(dto.getSeatNumber());
        seatRepository.save(seat);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update seat successful");
        return response;
    }

    @Transactional
    public Map<String, String> deleteSeat(Long seatId) {
        SeatModel seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seat not found"));

        String status = resolveSeatStatus(seatId);
        if ("LOCKED".equals(status) || "BOOKED".equals(status)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete seat that is locked or booked");
        }

        seatRepository.delete(seat);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete seat successful");
        return response;
    }

    @Transactional
    public Map<String, String> deleteSeatsByZone(Long zoneId) {
        ZoneModel zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Zone not found"));

        List<SeatModel> seats = seatRepository.findByZoneId(zone.getId());
        for (SeatModel s : seats) {
            String status = resolveSeatStatus(s.getId());
            if ("LOCKED".equals(status) || "BOOKED".equals(status)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Cannot delete: seat " + s.getRowLabel() + s.getSeatNumber() + " is locked or booked");
            }
        }
        seatRepository.deleteAll(seats);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete " + seats.size() + " seat(s) successful");
        return response;
    }

    private List<SeatResponse> mapSeats(List<SeatModel> seats) {
        return seats.stream().map(seat -> {
            String status = resolveSeatStatus(seat.getId());
            ZoneModel zone = seat.getZone();
            return new SeatResponse(
                    seat.getId(),
                    zone.getId(),
                    zone.getName(),
                    zone.getType() != null ? zone.getType().name() : null,
                    zone.getPrice(),
                    seat.getRowLabel(),
                    seat.getSeatNumber(),
                    status);
        }).toList();
    }

    private String resolveSeatStatus(Long seatId) {
        try {
            if (redisTemplate.opsForValue().get("seat:booked:" + seatId) != null) {
                return "BOOKED";
            }
            if (redisTemplate.opsForValue().get("seat:lock:" + seatId) != null) {
                return "LOCKED";
            }
        } catch (Exception ex) {
            // Redis is down — assume seat is available so the UI keeps working.
            // Locking integrity is enforced at order-creation time anyway.
        }
        return "AVAILABLE";
    }
}
