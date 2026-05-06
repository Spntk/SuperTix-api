package com.supertix.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supertix.api.dtos.seat.SeatCreateRequest;
import com.supertix.api.dtos.seat.SeatResponse;
import com.supertix.api.dtos.seat.SeatUpdateRequest;
import com.supertix.api.service.SeatService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/seat")
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createSeats(@Valid @RequestBody SeatCreateRequest dto) {
        return ResponseEntity.ok(seatService.createSeats(dto));
    }

    @GetMapping("/zone/{id}")
    public ResponseEntity<List<SeatResponse>> getSeatsByZone(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByZone(id));
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<List<SeatResponse>> getSeatsByEvent(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.getSeatsByEvent(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateSeat(@PathVariable Long id,
            @Valid @RequestBody SeatUpdateRequest dto) {
        return ResponseEntity.ok(seatService.updateSeat(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteSeat(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.deleteSeat(id));
    }

    @DeleteMapping("/zone/{id}")
    public ResponseEntity<Map<String, String>> deleteSeatsByZone(@PathVariable Long id) {
        return ResponseEntity.ok(seatService.deleteSeatsByZone(id));
    }
}
