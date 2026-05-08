package com.supertix.api.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.supertix.api.dtos.event.EventCreateRequest;
import com.supertix.api.dtos.event.EventResponse;
import com.supertix.api.dtos.event.EventUpdateRequest;
import com.supertix.api.enums.EventStatus;
import com.supertix.api.enums.VenueStatus;
import com.supertix.api.models.EventModel;
import com.supertix.api.models.VenueModel;
import com.supertix.api.repositories.EventRepository;
import com.supertix.api.repositories.VenueRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final FileUploadService fileUploadService;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository,
            FileUploadService fileUploadService) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public List<EventResponse> getAllEvent() {
        return eventRepository.findAll()
                .stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getVenue().getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getImageUrl(),
                        event.getVenue().getName(),
                        event.getStartDate().toString(),
                        event.getSaleStartDate().toString(),
                        event.getEndDate().toString(),
                        event.getStatus()))
                .toList();
    }

    @Transactional
    public EventResponse getEventById(Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event is not found"));

        return new EventResponse(
                event.getId(),
                event.getVenue().getId(),
                event.getTitle(),
                event.getDescription(),
                event.getImageUrl(),
                event.getVenue().getName(),
                event.getStartDate().toString(),
                event.getSaleStartDate().toString(),
                event.getEndDate().toString(),
                event.getStatus());
    }

    public Map<String, String> createEvent(EventCreateRequest dto) {
        VenueModel venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Venue is not found"));

        if (venue.getStatus() == VenueStatus.INACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venue is inactive");
        }

        EventModel event = new EventModel();
        event.setVenue(venue);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setImageUrl(dto.getImageUrl());
        event.setImageFileId(dto.getImageFileId());
        event.setStartDate(dto.getStartDate());
        event.setSaleStartDate(dto.getSaleStartDate());
        event.setEndDate(dto.getEndDate());
        event.setStatus(EventStatus.UPCOMING);
        eventRepository.save(event);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Create event successful");
        return response;
    }

    public Map<String, String> updateEventById(Long eventId, EventUpdateRequest dto) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event is not found"));

        VenueModel venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue is not found"));

        boolean hasNewImage = dto.getImageFileId() != null && !dto.getImageFileId().isEmpty();
        boolean hasOldImage = event.getImageFileId() != null && !event.getImageFileId().isEmpty();
        boolean imageChanged = hasNewImage && !dto.getImageFileId().equals(event.getImageFileId());

        if (imageChanged && hasOldImage) {
            fileUploadService.deleteFile(event.getImageFileId());
        }

        event.setVenue(venue);
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setImageUrl(dto.getImageUrl());
        event.setImageFileId(dto.getImageFileId());
        event.setStartDate(dto.getStartDate());
        event.setSaleStartDate(dto.getSaleStartDate());
        event.setEndDate(dto.getEndDate());
        event.setStatus(dto.getStatus());
        eventRepository.save(event);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Update event successful");
        return response;
    }

    public Map<String, String> cancelEventById(Long eventId) {
        EventModel event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event is not found"));

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Cancel event successful");
        return response;
    }
}
