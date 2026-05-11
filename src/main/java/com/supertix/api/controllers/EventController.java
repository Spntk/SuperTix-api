package com.supertix.api.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supertix.api.dtos.event.EventCreateRequest;
import com.supertix.api.dtos.event.EventDetailResponse;
import com.supertix.api.dtos.event.EventLayoutResponse;
import com.supertix.api.dtos.event.EventResponse;
import com.supertix.api.dtos.event.EventUpdateRequest;
import com.supertix.api.service.EventLayoutService;
import com.supertix.api.service.EventService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/event")
public class EventController {

    private final EventService eventService;
    private final EventLayoutService eventLayoutService;

    public EventController(EventService eventService, EventLayoutService eventLayoutService) {
        this.eventService = eventService;
        this.eventLayoutService = eventLayoutService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createEvent(@Valid @RequestBody EventCreateRequest dto) {
        return ResponseEntity.ok(eventService.createEvent(dto));
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvent() {
        return ResponseEntity.ok(eventService.getAllEvent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    /**
     * One-shot endpoint for the ticket-selection page.
     * Returns the event header, the venue layout canvas, every zone with its
     * position/orientation, and every seat with its current live status.
     */
    @GetMapping("/{id}/layout")
    public ResponseEntity<EventLayoutResponse.Payload> getEventLayout(@PathVariable Long id) {
        return ResponseEntity.ok(eventLayoutService.getEventLayout(id));
    }

    /**
     * Admin convenience: bulk-fills sensible default layout values for any
     * zone in this event that's missing them. Existing values are preserved.
     */
    @PostMapping("/{id}/layout/auto-arrange")
    public ResponseEntity<java.util.Map<String, Object>> autoArrangeLayout(@PathVariable Long id) {
        return ResponseEntity.ok(eventLayoutService.autoArrange(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateEventById(@PathVariable Long id,
            @Valid @RequestBody EventUpdateRequest dto) {
        return ResponseEntity.ok(eventService.updateEventById(id, dto));
    }

    @PatchMapping("/cancel/{id}")
    public ResponseEntity<Map<String, String>> cancelEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.cancelEventById(id));
    }
}
