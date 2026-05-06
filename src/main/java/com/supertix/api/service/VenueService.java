package com.supertix.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.venue.VenueCreateRequest;
import com.supertix.api.dtos.venue.VenueResponse;
import com.supertix.api.dtos.venue.VenueUpdateRequest;
import com.supertix.api.models.VenueModel;
import com.supertix.api.repositories.VenueRepository;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAllByOrderByIdAsc()
                .stream()
                .map(venue -> new VenueResponse(
                        venue.getId(),
                        venue.getName(),
                        venue.getCapacity(),
                        venue.getImageUrl(),
                        venue.getStatus().name(),
                        venue.getCreatedAt().toString()))
                .toList();
    }

    public Map<String, String> createVenue(VenueCreateRequest dto) {
        if (venueRepository.existsByName(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanue name already exists");
        }

        VenueModel venue = new VenueModel();
        venue.setName(dto.getName());
        venue.setCapacity(dto.getCapacity());
        venue.setImageUrl(dto.getImageUrl());
        venueRepository.save(venue);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Create venue successful");
        return response;
    }

    public Map<String, String> updateVenueById(Long venueId, VenueUpdateRequest dto) {
        VenueModel venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        venue.setName(dto.getName());
        venue.setCapacity(dto.getCapacity());
        venue.setImageUrl(dto.getImageUrl());
        venue.setStatus(dto.getStatus());
        venueRepository.save(venue);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update venue Successful");
        return response;
    }

    public Map<String, String> deleteVunue(Long venueId) {
        VenueModel venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        venueRepository.delete(venue);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete venue Successful");
        return response;
    }
}
