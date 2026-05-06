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

import com.supertix.api.dtos.venue.VenueCreateRequest;
import com.supertix.api.dtos.venue.VenueResponse;
import com.supertix.api.dtos.venue.VenueUpdateRequest;
import com.supertix.api.service.VenueService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/venue")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createVenue(@Valid @RequestBody VenueCreateRequest dto) {
        return ResponseEntity.ok(venueService.createVenue(dto));
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateVenueById(@PathVariable Long id,
            @Valid @RequestBody VenueUpdateRequest dto) {
        return ResponseEntity.ok(venueService.updateVenueById(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteVenue(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.deleteVunue(id));
    }
}
