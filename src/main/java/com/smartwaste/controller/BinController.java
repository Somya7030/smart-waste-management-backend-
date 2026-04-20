package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.dto.BinDTO;
import com.smartwaste.dto.BinUpdateRequest;
import com.smartwaste.model.BinStatus;
import com.smartwaste.service.BinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bins")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BinController {

    private final BinService binService;

    /**
     * GET /api/bins
     * Returns all bins. Optionally filter by status.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BinDTO>>> getAllBins(
            @RequestParam(required = false) BinStatus status) {
        if (status != null) {
            return ResponseEntity.ok(
                    ApiResponse.success("Bins filtered by status: " + status,
                            binService.getBinsByStatus(status)));
        }
        return ResponseEntity.ok(ApiResponse.success(binService.getAllBins()));
    }

    /**
     * GET /api/bins/{id}
     * Returns a single bin by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BinDTO>> getBinById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(binService.getBinById(id)));
    }

    /**
     * GET /api/bins/code/{binCode}
     * Returns a single bin by its unique code.
     */
    @GetMapping("/code/{binCode}")
    public ResponseEntity<ApiResponse<BinDTO>> getBinByCode(@PathVariable String binCode) {
        return ResponseEntity.ok(ApiResponse.success(binService.getBinByCode(binCode)));
    }

    /**
     * POST /api/bins/update
     * Updates (or auto-creates) a bin's fill level and recalculates status.
     * Used by IoT devices and the MQTT pipeline.
     */
    @PostMapping("/update")
    public ResponseEntity<ApiResponse<BinDTO>> updateBin(
            @Valid @RequestBody BinUpdateRequest request) {
        log.info("REST update received for bin: {}", request.getBinCode());
        BinDTO updated = binService.updateBin(request);
        return ResponseEntity.ok(
                ApiResponse.success("Bin updated successfully", updated));
    }
}
