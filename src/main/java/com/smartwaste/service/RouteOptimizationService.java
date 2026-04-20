package com.smartwaste.service;

import com.smartwaste.dto.RouteOptimizationRequest;
import com.smartwaste.dto.RouteOptimizationResponse;
import com.smartwaste.dto.RouteOptimizationResponse.RouteCluster;
import com.smartwaste.dto.RouteOptimizationResponse.RouteBinStop;
import com.smartwaste.model.Bin;
import com.smartwaste.model.BinStatus;
import com.smartwaste.repository.BinRepository;
import com.smartwaste.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ML-Inspired Route Optimization Engine
 *
 * Algorithm:
 * 1. Feature Engineering  — compute priority score per bin
 *      priority = (fillLevel × W_fill) + (FULL_bonus if FULL) + (time_decay_factor)
 *
 * 2. KMeans-inspired Clustering — group nearby bins into zones
 *      - Iteratively assigns bins to nearest centroid
 *      - Recomputes centroid until convergence
 *
 * 3. Intra-cluster Sorting — within each cluster, sort by priority DESC
 *
 * 4. Route Sequencing — Depot → Cluster 1 bins → Cluster 2 bins → ... → Return Depot
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final BinRepository binRepository;

    @Value("${route.weight-fill-level:0.7}")
    private double weightFillLevel;

    @Value("${route.weight-full-bonus:50.0}")
    private double weightFullBonus;

    @Value("${route.cluster-radius-km:2.0}")
    private double defaultClusterRadiusKm;

    private static final double DEPOT_DEFAULT_LAT = 12.9716;
    private static final double DEPOT_DEFAULT_LON = 77.5946; // Bangalore, India

    private static final int MAX_KMEANS_ITERATIONS = 100;
    private static final int TARGET_CLUSTER_SIZE   = 5;     // ~5 bins per truck route

    public RouteOptimizationResponse optimizeRoute(RouteOptimizationRequest request) {
        log.info("Starting route optimization. fullBinsOnly={}", request.isFullBinsOnly());

        double depotLat = request.getDepotLatitude()  != null ? request.getDepotLatitude()  : DEPOT_DEFAULT_LAT;
        double depotLon = request.getDepotLongitude() != null ? request.getDepotLongitude() : DEPOT_DEFAULT_LON;
        double clusterRadius = request.getClusterRadiusKm() != null ? request.getClusterRadiusKm() : defaultClusterRadiusKm;

        // Step 1: Load & filter bins
        List<Bin> allBins = binRepository.findAll();
        List<Bin> targetBins = allBins.stream()
                .filter(b -> !request.isFullBinsOnly() || b.getStatus() == BinStatus.FULL)
                .filter(b -> b.getFillLevel() > 0)
                .collect(Collectors.toList());

        if (targetBins.isEmpty()) {
            log.warn("No bins to optimize route for.");
            return RouteOptimizationResponse.builder()
                    .clusters(Collections.emptyList())
                    .totalBins(0)
                    .totalDistance(0.0)
                    .estimatedTime("0 min")
                    .routeSummary("No bins require collection at this time.")
                    .build();
        }

        // Limit if requested
        if (request.getMaxBinsPerRoute() != null && request.getMaxBinsPerRoute() > 0) {
            targetBins = targetBins.stream()
                    .sorted(Comparator.comparingDouble(this::computePriority).reversed())
                    .limit(request.getMaxBinsPerRoute())
                    .collect(Collectors.toList());
        }

        // Step 2: Feature Engineering — compute priority per bin
        Map<Long, Double> priorityMap = new HashMap<>();
        for (Bin bin : targetBins) {
            priorityMap.put(bin.getId(), computePriority(bin));
        }

        // Step 3: KMeans Clustering
        int k = Math.max(1, (int) Math.ceil((double) targetBins.size() / TARGET_CLUSTER_SIZE));
        List<List<Bin>> clusters = kMeansClustering(targetBins, k, MAX_KMEANS_ITERATIONS);

        // Step 4: Sort clusters by proximity to depot (nearest first)
        clusters.sort(Comparator.comparingDouble(cluster -> {
            double[] centroid = computeCentroid(cluster);
            return GeoUtils.haversineDistance(depotLat, depotLon, centroid[0], centroid[1]);
        }));

        // Step 5: Sort bins within each cluster by priority (highest first)
        List<RouteCluster> routeClusters = new ArrayList<>();
        double totalDistance = 0.0;
        int globalStopOrder = 1;

        double currentLat = depotLat;
        double currentLon = depotLon;

        for (int ci = 0; ci < clusters.size(); ci++) {
            List<Bin> clusterBins = clusters.get(ci);

            // Sort by priority descending
            clusterBins.sort(Comparator.comparingDouble(
                    (Bin b) -> priorityMap.getOrDefault(b.getId(), 0.0)).reversed());

            List<RouteBinStop> stops = new ArrayList<>();
            double[] centroid = computeCentroid(clusterBins);
            double clusterPriority = clusterBins.stream()
                    .mapToDouble(b -> priorityMap.getOrDefault(b.getId(), 0.0))
                    .average().orElse(0.0);

            for (Bin bin : clusterBins) {
                double dist = GeoUtils.haversineDistance(currentLat, currentLon, bin.getLatitude(), bin.getLongitude());
                totalDistance += dist;
                currentLat = bin.getLatitude();
                currentLon = bin.getLongitude();

                stops.add(RouteBinStop.builder()
                        .stopOrder(globalStopOrder++)
                        .binId(bin.getId())
                        .binCode(bin.getBinCode())
                        .location(bin.getLocation())
                        .latitude(bin.getLatitude())
                        .longitude(bin.getLongitude())
                        .fillLevel(bin.getFillLevel())
                        .status(bin.getStatus().name())
                        .priority(Math.round(priorityMap.getOrDefault(bin.getId(), 0.0) * 10.0) / 10.0)
                        .stopType("BIN")
                        .build());
            }

            routeClusters.add(RouteCluster.builder()
                    .clusterIndex(ci + 1)
                    .clusterLabel("Zone-" + (char)('A' + ci))
                    .stops(stops)
                    .clusterCenterLat(Math.round(centroid[0] * 1e6) / 1e6)
                    .clusterCenterLon(Math.round(centroid[1] * 1e6) / 1e6)
                    .clusterPriority(Math.round(clusterPriority * 10.0) / 10.0)
                    .build());
        }

        // Add return distance to depot
        totalDistance += GeoUtils.haversineDistance(currentLat, currentLon, depotLat, depotLon);
        totalDistance = Math.round(totalDistance * 10.0) / 10.0;

        // Estimated time: assume avg 30 km/h + 5 min per bin
        int travelMinutes = (int)(totalDistance / 30.0 * 60);
        int serviceMinutes = targetBins.size() * 5;
        int totalMinutes = travelMinutes + serviceMinutes;

        String estimatedTime = totalMinutes >= 60
                ? String.format("%dh %dmin", totalMinutes / 60, totalMinutes % 60)
                : totalMinutes + " min";

        String summary = String.format(
                "Optimized route: %d bins across %d zones | Distance: %.1f km | ETA: %s | Starting from Depot (%.4f, %.4f)",
                targetBins.size(), clusters.size(), totalDistance, estimatedTime, depotLat, depotLon);

        log.info("Route optimization complete: {} bins, {} clusters, {:.1f} km", targetBins.size(), clusters.size(), totalDistance);

        return RouteOptimizationResponse.builder()
                .clusters(routeClusters)
                .totalBins(targetBins.size())
                .totalDistance(totalDistance)
                .estimatedTime(estimatedTime)
                .routeSummary(summary)
                .build();
    }

    // ─── Feature Engineering ─────────────────────────────────────────────

    /**
     * Priority score formula:
     *   priority = (fillLevel × W_fill) + (FULL_bonus if FULL) + time_weight
     *
     * A FULL bin at 95% fill gets significantly higher priority than a HALF bin at 70%.
     */
    private double computePriority(Bin bin) {
        double fillScore = bin.getFillLevel() * weightFillLevel;
        double fullBonus = (bin.getStatus() == BinStatus.FULL) ? weightFullBonus : 0.0;

        // Time decay: bins not updated for longer get slight urgency boost
        long hoursSinceUpdate = java.time.Duration.between(
                bin.getLastUpdatedTime(),
                java.time.LocalDateTime.now()).toHours();
        double timeWeight = Math.min(hoursSinceUpdate * 0.5, 20.0); // cap at 20

        return fillScore + fullBonus + timeWeight;
    }

    // ─── KMeans Clustering ───────────────────────────────────────────────

    /**
     * KMeans-inspired geospatial clustering.
     * Bins are assigned to the nearest centroid each iteration.
     * Centroids are recomputed as the average lat/lon of assigned bins.
     */
    private List<List<Bin>> kMeansClustering(List<Bin> bins, int k, int maxIterations) {
        if (bins.size() <= k) {
            // Each bin is its own cluster
            return bins.stream()
                    .map(b -> new ArrayList<>(List.of(b)))
                    .collect(Collectors.toList());
        }

        // Initialize centroids using KMeans++ strategy (spread-out initial seeds)
        List<double[]> centroids = initializeCentroidsKMeansPlusPlus(bins, k);

        List<List<Bin>> clusters = new ArrayList<>();
        for (int iter = 0; iter < maxIterations; iter++) {
            // Assignment step
            clusters = new ArrayList<>();
            for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

            for (Bin bin : bins) {
                int nearest = findNearestCentroid(bin, centroids);
                clusters.get(nearest).add(bin);
            }

            // Remove empty clusters
            clusters.removeIf(List::isEmpty);
            if (clusters.size() < k) k = clusters.size();

            // Update step: recompute centroids
            List<double[]> newCentroids = clusters.stream()
                    .map(this::computeCentroid)
                    .collect(Collectors.toList());

            // Check convergence
            if (centroidsConverged(centroids, newCentroids)) {
                log.debug("KMeans converged at iteration {}", iter + 1);
                break;
            }
            centroids = newCentroids;
        }

        return clusters;
    }

    private List<double[]> initializeCentroidsKMeansPlusPlus(List<Bin> bins, int k) {
        List<double[]> centroids = new ArrayList<>();
        Random random = new Random(42); // fixed seed for reproducibility

        // First centroid: random bin
        Bin first = bins.get(random.nextInt(bins.size()));
        centroids.add(new double[]{first.getLatitude(), first.getLongitude()});

        // Subsequent centroids: choose with probability proportional to D^2
        for (int i = 1; i < k; i++) {
            double[] distances = new double[bins.size()];
            double total = 0;
            for (int j = 0; j < bins.size(); j++) {
                Bin bin = bins.get(j);
                double minDist = centroids.stream()
                        .mapToDouble(c -> GeoUtils.haversineDistance(
                                bin.getLatitude(), bin.getLongitude(), c[0], c[1]))
                        .min().orElse(0.0);
                distances[j] = minDist * minDist;
                total += distances[j];
            }
            double threshold = random.nextDouble() * total;
            double cumulative = 0;
            for (int j = 0; j < bins.size(); j++) {
                cumulative += distances[j];
                if (cumulative >= threshold) {
                    Bin chosen = bins.get(j);
                    centroids.add(new double[]{chosen.getLatitude(), chosen.getLongitude()});
                    break;
                }
            }
        }
        return centroids;
    }

    private int findNearestCentroid(Bin bin, List<double[]> centroids) {
        int nearest = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < centroids.size(); i++) {
            double dist = GeoUtils.haversineDistance(
                    bin.getLatitude(), bin.getLongitude(),
                    centroids.get(i)[0], centroids.get(i)[1]);
            if (dist < minDist) {
                minDist = dist;
                nearest = i;
            }
        }
        return nearest;
    }

    private double[] computeCentroid(List<Bin> cluster) {
        double avgLat = cluster.stream().mapToDouble(Bin::getLatitude).average().orElse(0);
        double avgLon = cluster.stream().mapToDouble(Bin::getLongitude).average().orElse(0);
        return new double[]{avgLat, avgLon};
    }

    private boolean centroidsConverged(List<double[]> prev, List<double[]> next) {
        if (prev.size() != next.size()) return false;
        for (int i = 0; i < prev.size(); i++) {
            if (GeoUtils.haversineDistance(prev.get(i)[0], prev.get(i)[1],
                    next.get(i)[0], next.get(i)[1]) > 0.01) {
                return false;
            }
        }
        return true;
    }
}
