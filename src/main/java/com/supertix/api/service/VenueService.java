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
    private final FileUploadService fileUploadService;

    public VenueService(VenueRepository venueRepository, FileUploadService fileUploadService) {
        this.venueRepository = venueRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAllByOrderByIdAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Map<String, String> createVenue(VenueCreateRequest dto) {
        if (venueRepository.existsByName(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vanue name already exists");
        }

        VenueModel venue = new VenueModel();
        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setCapacity(dto.getCapacity());
        venue.setImageUrl(dto.getImageUrl());
        venue.setImageFileId(dto.getImageFileId());
        venue.setStatus(dto.getStatus());
        if (dto.getLayoutWidth() != null) venue.setLayoutWidth(dto.getLayoutWidth());
        if (dto.getLayoutHeight() != null) venue.setLayoutHeight(dto.getLayoutHeight());
        venue.setMapImageUrl(dto.getMapImageUrl());
        venue.setStageX(dto.getStageX());
        venue.setStageY(dto.getStageY());
        venue.setStageWidth(dto.getStageWidth());
        venue.setStageHeight(dto.getStageHeight());
        venueRepository.save(venue);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Create venue successful");
        return response;
    }

    public Map<String, String> updateVenueById(Long venueId, VenueUpdateRequest dto) {
        VenueModel venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        boolean hasNewImage = dto.getImageFileId() != null && !dto.getImageFileId().isEmpty();
        boolean hasOldImage = venue.getImageFileId() != null && !venue.getImageFileId().isEmpty();
        boolean imageChanged = hasNewImage && !dto.getImageFileId().equals(venue.getImageFileId());

        if (imageChanged && hasOldImage) {
            fileUploadService.deleteFile(venue.getImageFileId());
        }

        venue.setName(dto.getName());
        venue.setAddress(dto.getAddress());
        venue.setCapacity(dto.getCapacity());
        venue.setImageUrl(dto.getImageUrl());
        venue.setImageFileId(dto.getImageFileId());
        venue.setStatus(dto.getStatus());
        if (dto.getLayoutWidth() != null) venue.setLayoutWidth(dto.getLayoutWidth());
        if (dto.getLayoutHeight() != null) venue.setLayoutHeight(dto.getLayoutHeight());
        if (dto.getMapImageUrl() != null) venue.setMapImageUrl(dto.getMapImageUrl());
        if (dto.getStageX() != null) venue.setStageX(dto.getStageX());
        if (dto.getStageY() != null) venue.setStageY(dto.getStageY());
        if (dto.getStageWidth() != null) venue.setStageWidth(dto.getStageWidth());
        if (dto.getStageHeight() != null) venue.setStageHeight(dto.getStageHeight());
        venueRepository.save(venue);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update venue Successful");
        return response;
    }

    public Map<String, String> deleteVunue(Long venueId) {
        VenueModel venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        if (venue.getImageFileId() != null) {
            fileUploadService.deleteFile(venue.getImageFileId());
        }
        venueRepository.delete(venue);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Delete venue Successful");
        return response;
    }

    private VenueResponse toResponse(VenueModel venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCapacity(),
                venue.getImageUrl(),
                venue.getStatus().name(),
                venue.getCreatedAt().toString(),
                venue.getLayoutWidth(),
                venue.getLayoutHeight(),
                venue.getMapImageUrl(),
                venue.getStageX(),
                venue.getStageY(),
                venue.getStageWidth(),
                venue.getStageHeight());
    }
}
