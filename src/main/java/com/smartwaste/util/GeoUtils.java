package com.smartwaste.util;

/**
 * Geographic utility functions.
 */
public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}

    /**
     * Calculates the great-circle distance between two coordinates
     * using the Haversine formula.
     *
     * @return Distance in kilometers
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * Converts degrees to radians.
     */
    public static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    /**
     * Checks whether a coordinate is within a given radius (km) of a center point.
     */
    public static boolean isWithinRadius(double centerLat, double centerLon,
                                          double pointLat, double pointLon,
                                          double radiusKm) {
        return haversineDistance(centerLat, centerLon, pointLat, pointLon) <= radiusKm;
    }
}
