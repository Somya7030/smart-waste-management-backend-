package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.dto.WasteClassificationDTO;
import com.smartwaste.service.WasteClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/classify")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WasteClassificationController {

    private final WasteClassificationService classificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * POST /api/classify
     * Accept a waste image and return AI classification result.
     *
     * Form field:
     *   image (MultipartFile, required)
     *
     * Response:
     * {
     *   "wasteType": "Plastic",
     *   "confidence": 0.92,
     *   "confidencePercent": "92%",
     *   "disposalInstructions": "Place in blue recycling bin...",
     *   "recyclable": "Yes - Check resin code"
     * }
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<WasteClassificationDTO>> classifyWaste(
            @RequestParam("image") MultipartFile image) {
        log.info("Classification request received for: {}", image.getOriginalFilename());
        WasteClassificationDTO result = classificationService.classifyWaste(image);
        return ResponseEntity.ok(
                ApiResponse.success("Waste classified successfully", result));
    }

    /**
     * GET /api/classify/image/{fileName}
     * Serves uploaded classification images.
     */
    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> serveImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
