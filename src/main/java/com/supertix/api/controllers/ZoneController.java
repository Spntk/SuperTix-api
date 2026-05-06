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

import com.supertix.api.dtos.zone.ZoneCreateRequest;
import com.supertix.api.dtos.zone.ZoneResponse;
import com.supertix.api.dtos.zone.ZoneUpdateRequest;
import com.supertix.api.service.ZoneService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/zone")
public class ZoneController {

    private final ZoneService zoneService;

    public ZoneController(ZoneService zoneService) {
        this.zoneService = zoneService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createZone(@Valid @RequestBody ZoneCreateRequest dto) {
        return ResponseEntity.ok(zoneService.createZone(dto));
    }

    @GetMapping("/event/{id}")
    public ResponseEntity<List<ZoneResponse>> getZonesByEvent(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.getZonesByEvent(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, String>> updateZone(@PathVariable Long id,
            @Valid @RequestBody ZoneUpdateRequest dto) {
        return ResponseEntity.ok(zoneService.updateZone(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteZone(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.deleteZone(id));
    }
}
