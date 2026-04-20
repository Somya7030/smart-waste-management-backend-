package com.smartwaste.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class RouteService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ================= ML CLUSTER CALL =================
    public Map<Integer, List<String>> getClusters(List<Map<String, Object>> bins) {

        try {
            String url = "http://localhost:5000/optimize";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<List<Map<String, Object>>> request =
                    new HttpEntity<>(bins, headers);

            String response = restTemplate.postForObject(url, request, String.class);

            List<Map<String, Object>> result =
                    objectMapper.readValue(response, List.class);

            Map<Integer, List<String>> clusters = new HashMap<>();

            for (Map<String, Object> item : result) {
                int cluster = (int) item.get("cluster");
                String binCode = (String) item.get("binCode");

                clusters.computeIfAbsent(cluster, k -> new ArrayList<>()).add(binCode);
            }

            return clusters;

        } catch (Exception e) {
            throw new RuntimeException("ML service error", e);
        }
    }

    // ================= DISTANCE FUNCTION =================
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ================= SMART ROUTE OPTIMIZATION =================
    public List<Map<String, Object>> optimizeClusterRoute(List<Map<String, Object>> bins) {

        List<Map<String, Object>> route = new ArrayList<>();

        if (bins.isEmpty()) return route;

        // STEP 1: start with highest fillLevel
        bins.sort((a, b) -> Double.compare(
                (double) b.get("fillLevel"),
                (double) a.get("fillLevel")
        ));

        Map<String, Object> current = bins.remove(0);
        route.add(current);

        // STEP 2: smart selection (fillLevel + distance)
        while (!bins.isEmpty()) {

            Map<String, Object> bestBin = null;
            double bestScore = Double.NEGATIVE_INFINITY;

            for (Map<String, Object> b : bins) {

                double dist = calculateDistance(
                        (double) current.get("latitude"),
                        (double) current.get("longitude"),
                        (double) b.get("latitude"),
                        (double) b.get("longitude")
                );

                double fill = (double) b.get("fillLevel");

                // 🔥 SCORE FORMULA
                double score = fill - dist;

                if (score > bestScore) {
                    bestScore = score;
                    bestBin = b;
                }
            }

            route.add(bestBin);
            bins.remove(bestBin);
            current = bestBin;
        }

        return route;
    }
}