package com.smartwaste.service;

import com.smartwaste.dto.WasteClassificationDTO;
import com.smartwaste.exception.FileStorageException;
import com.smartwaste.model.WasteClassification;
import com.smartwaste.repository.WasteClassificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class WasteClassificationService {

    private final WasteClassificationRepository classificationRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.classify-api-url:http://localhost:5001/classify}")
    private String mlApiUrl;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final Map<String, String> DISPOSAL_INSTRUCTIONS = Map.of(
            "plastic",      "Place in blue recycling bin. Rinse before disposal.",
            "organic",      "Compost or place in green bin. Do not mix with recyclables.",
            "metal",        "Place in recycling bin. Remove food residue.",
            "paper",        "Flatten and place in recycling. Keep dry.",
            "glass",        "Place in glass recycling bin. Handle carefully.",
            "medical",      "Take to designated medical waste collection point.",
            "unknown",      "Follow local municipal disposal guidelines."
    );

    private static final Map<String, String> RECYCLABLE_MAP = Map.of(
            "plastic",  "Yes - Check resin code",
            "organic",  "Compostable",
            "metal",    "Yes",
            "paper",    "Yes - If not soiled",
            "glass",    "Yes",
            "medical",  "No - Special Disposal",
            "unknown",  "Unknown"
    );

    @Transactional
    public WasteClassificationDTO classifyWaste(MultipartFile image) {
        log.info("Classifying waste image: {}", image.getOriginalFilename());

        // Save file first so we can send it to Python
        String savedFileName = storeFile(image);
        Path filePath = Paths.get(uploadDir).toAbsolutePath()
                             .normalize().resolve(savedFileName);

        // Call Python Flask ML service
        ClassificationResult result = callPythonClassifier(filePath);

        // Save to DB
        WasteClassification classification = WasteClassification.builder()
                .imagePath(savedFileName)
                .wasteType(result.category())
                .confidence(result.confidence())
                .additionalInfo("Classified via YOLOv8 + Roboflow pipeline. Source: " + result.source())
                .build();

        WasteClassification saved = classificationRepository.save(classification);

        String confidencePercent = String.format("%.0f%%", result.confidence() * 100);
        log.info("Classification result: {} ({}) via {}", result.category(), confidencePercent, result.source());

        String category = result.category().toLowerCase();

        return WasteClassificationDTO.builder()
                .id(saved.getId())
                .wasteType(saved.getWasteType())
                .confidence(saved.getConfidence())
                .confidencePercent(confidencePercent)
                .imageUrl("/api/classify/image/" + savedFileName)
                .additionalInfo(saved.getAdditionalInfo())
                .classifiedAt(saved.getClassifiedAt())
                .disposalInstructions(DISPOSAL_INSTRUCTIONS.getOrDefault(category, "Follow local guidelines."))
                .recyclable(RECYCLABLE_MAP.getOrDefault(category, "Unknown"))
                .build();
    }

    /**
     * Calls Python Flask /classify endpoint with the image file.
     * Falls back to "unknown" if the service is unreachable.
     */
    private ClassificationResult callPythonClassifier(Path imagePath) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(imagePath));

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    mlApiUrl, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                String category  = String.valueOf(responseBody.getOrDefault("category", "unknown"));
                double confidence = parseConfidence(responseBody.get("confidence"));
                String source    = String.valueOf(responseBody.getOrDefault("source", "python"));
                return new ClassificationResult(category, confidence, source);
            }

        } catch (Exception e) {
            log.warn("Python ML service unreachable at {}. Error: {}", mlApiUrl, e.getMessage());
        }

        // Fallback if Python is down
        return new ClassificationResult("unknown", 0.0, "fallback");
    }

    private double parseConfidence(Object raw) {
        if (raw == null) return 0.75; // default confidence
        try { return Double.parseDouble(raw.toString()); }
        catch (NumberFormatException e) { return 0.75; }
    }

    private String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) extension = originalFileName.substring(dotIndex);
        String storedFileName = "classify_" + UUID.randomUUID() + extension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            Path targetLocation = uploadPath.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation,
                       StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store classification image", ex);
        }
    }

    private record ClassificationResult(String category, double confidence, String source) {}
}