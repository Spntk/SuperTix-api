package com.supertix.api.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.supertix.api.service.FileUploadService;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/event")
    public ResponseEntity<Map<String, String>> uploadEventImage(
            @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadEventImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/venue")
    public ResponseEntity<Map<String, String>> uploadVenueImage(
            @RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadVenueImage(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
