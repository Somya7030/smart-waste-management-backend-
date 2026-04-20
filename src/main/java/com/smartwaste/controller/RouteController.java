package com.smartwaste.controller;

import com.smartwaste.dto.BinDTO;
import com.smartwaste.service.BinService;
import com.smartwaste.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;
    private final BinService binService;

    @GetMapping("/optimize")
    public Map<String, Object> optimize() {

        // 1. Get all bins
        List<BinDTO> bins = binService.getAllBins();

        // 2. Filter important bins
        List<BinDTO> filtered = bins.stream()
                .filter(b -> b.getFillLevel() > 60 && b.getLatitude() != 0)
                .toList();

        // 3. Prepare data for ML
        List<Map<String, Object>> simpleBins = filtered.stream()
                .map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("binCode", b.getBinCode());
                    map.put("latitude", b.getLatitude());
                    map.put("longitude", b.getLongitude());
                    return map;
                })
                .toList();

        // 4. Call ML service
        Map<Integer, List<String>> clusters = routeService.getClusters(simpleBins);

        // 5. Create bin lookup map
        Map<String, BinDTO> binMap = new HashMap<>();
        for (BinDTO b : filtered) {
            binMap.put(b.getBinCode(), b);
        }

        // 6. Build final routes
        List<Map<String, Object>> routes = new ArrayList<>();

        for (Map.Entry<Integer, List<String>> entry : clusters.entrySet()) {

            int clusterId = entry.getKey();
            List<String> binCodes = entry.getValue();

            List<Map<String, Object>> clusterList = new ArrayList<>();

            for (String code : binCodes) {
                BinDTO b = binMap.get(code);

                Map<String, Object> map = new HashMap<>();
                map.put("binCode", b.getBinCode());
                map.put("fillLevel", b.getFillLevel());
                map.put("latitude", b.getLatitude());
                map.put("longitude", b.getLongitude());

                clusterList.add(map);
            }

            // 🔥 CALL OPTIMIZATION
            List<Map<String, Object>> optimizedRoute =
                    routeService.optimizeClusterRoute(clusterList);

            Map<String, Object> clusterData = new HashMap<>();
            clusterData.put("cluster", clusterId);
            clusterData.put("route", optimizedRoute);

            routes.add(clusterData);
        }
        // 7. Final response
        Map<String, Object> response = new HashMap<>();
        response.put("routes", routes);

        return response;
    }
}