package com.smartwaste.service;

import com.smartwaste.dto.BinDTO;
import com.smartwaste.dto.BinUpdateRequest;
import com.smartwaste.dto.MapBinDTO;
import com.smartwaste.exception.ResourceNotFoundException;
import com.smartwaste.model.Bin;
import com.smartwaste.model.BinStatus;
import com.smartwaste.repository.BinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BinService {

    private final BinRepository binRepository;
    private final AlertService alertService;

    public List<BinDTO> getAllBins() {
        log.debug("Fetching all bins");
        return binRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BinDTO getBinById(Long id) {
        log.debug("Fetching bin by id: {}", id);
        Bin bin = binRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with id: " + id));
        return toDTO(bin);
    }

    public BinDTO getBinByCode(String binCode) {
        Bin bin = binRepository.findByBinCode(binCode)
                .orElseThrow(() -> new ResourceNotFoundException("Bin not found with code: " + binCode));
        return toDTO(bin);
    }

    @Transactional
    public BinDTO updateBin(BinUpdateRequest request) {
        log.info("Updating bin: {} with fillLevel: {}", request.getBinCode(), request.getFillLevel());

        Bin bin = binRepository.findByBinCode(request.getBinCode())
                .orElseGet(() -> {
                    log.info("Bin not found, creating new bin with code: {}", request.getBinCode());
                    return Bin.builder()
                            .binCode(request.getBinCode())
                            .location(request.getLocation() != null ? request.getLocation() : "Unknown")
                            .latitude(request.getLatitude() != null ? request.getLatitude() : 0.0)
                            .longitude(request.getLongitude() != null ? request.getLongitude() : 0.0)
                            .status(BinStatus.EMPTY)
                            .build();
                });

        bin.setFillLevel(request.getFillLevel());
        if (request.getLatitude() != null) bin.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) bin.setLongitude(request.getLongitude());
        if (request.getLocation() != null) bin.setLocation(request.getLocation());
        bin.setLastUpdatedTime(LocalDateTime.now());
        bin.recalculateStatus();

        Bin saved = binRepository.save(bin);

        // Trigger alert if FULL
        if (saved.getStatus() == BinStatus.FULL) {
            alertService.createAlertIfNotExists(saved);
        } else {
            // Resolve any existing alerts if bin is emptied
            alertService.resolveAlerts(saved);
        }

        log.info("Bin {} updated to status: {}", saved.getBinCode(), saved.getStatus());
        return toDTO(saved);
    }

    public List<MapBinDTO> getAllBinsForMap() {
        return binRepository.findAll()
                .stream()
                .map(this::toMapDTO)
                .collect(Collectors.toList());
    }

    public List<BinDTO> getBinsByStatus(BinStatus status) {
        return binRepository.findByStatus(status)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<Bin> getAllBinEntities() {
        return binRepository.findAll();
    }

    // ─── Mappers ─────────────────────────────────────────────────────────

    public BinDTO toDTO(Bin bin) {
        return BinDTO.builder()
                .id(bin.getId())
                .binCode(bin.getBinCode())
                .location(bin.getLocation())
                .fillLevel(bin.getFillLevel())
                .status(bin.getStatus())
                .latitude(bin.getLatitude())
                .longitude(bin.getLongitude())
                .lastUpdatedTime(bin.getLastUpdatedTime())
                .build();
    }

    public MapBinDTO toMapDTO(Bin bin) {
        return MapBinDTO.builder()
                .binId(bin.getId())
                .binCode(bin.getBinCode())
                .latitude(bin.getLatitude())
                .longitude(bin.getLongitude())
                .status(bin.getStatus())
                .fillLevel(bin.getFillLevel())
                .location(bin.getLocation())
                .build();
    }
}
