package com.supertix.api.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.config.Configuration;
import io.imagekit.sdk.models.FileCreateRequest;
import io.imagekit.sdk.models.results.Result;

@Service
public class FileUploadService {

    @Value("${imagekit.private-key}")
    private String privateKey;

    @Value("${imagekit.public-key}")
    private String publicKey;

    @Value("${imagekit.url-endpoint}")
    private String urlEndpoint;

    public String uploadEventImage(MultipartFile file) {
        return uploadToFolder(file, "/events");
    }

    public String uploadVenueImage(MultipartFile file) {
        return uploadToFolder(file, "/venues");
    }

    private String uploadToFolder(MultipartFile file, String folder) {
        try {
            ImageKit imageKit = ImageKit.getInstance();
            Configuration config = new Configuration(publicKey, privateKey, urlEndpoint);
            imageKit.setConfig(config);

            FileCreateRequest request = new FileCreateRequest(
                    file.getBytes(),
                    UUID.randomUUID() + "_" + file.getOriginalFilename());
            request.setFolder(folder);

            Result result = imageKit.upload(request);
            return result.getUrl();
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file");
        }
    }
}
