package com.smartwaste.controller;

import com.smartwaste.dto.ApiResponse;
import com.smartwaste.dto.MapBinDTO;
import com.smartwaste.service.BinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MapController {

    private final BinService binService;

    /**
     * GET /api/map/bins
     * Returns lightweight bin data for map rendering:
     * binId, binCode, latitude, longitude, status, fillLevel, location
     */
    @GetMapping("/bins")
    public ResponseEntity<ApiResponse<List<MapBinDTO>>> getBinsForMap() {
        return ResponseEntity.ok(
                ApiResponse.success("Map bin data retrieved", binService.getAllBinsForMap()));
    }
}
